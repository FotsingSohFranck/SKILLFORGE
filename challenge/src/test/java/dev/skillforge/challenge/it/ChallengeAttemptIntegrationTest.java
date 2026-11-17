package dev.skillforge.challenge.it;

import dev.skillforge.challenge.ScenarioFixtures;
import dev.skillforge.challenge.domain.model.AttemptStatus;
import dev.skillforge.challenge.domain.model.AttemptView;
import dev.skillforge.challenge.domain.port.in.GetAttemptUseCase;
import dev.skillforge.challenge.domain.port.in.ImportScenarioUseCase;
import dev.skillforge.challenge.domain.port.in.StartAttemptUseCase;
import dev.skillforge.challenge.domain.port.in.SubmitDecisionUseCase;
import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.common.error.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test d'intégration du moteur de tentative (SF-92a-2) contre PostgreSQL 18 : cycle complet
 * import → démarrer → décider → rapport, persistance et relecture, propriété de la tentative,
 * et rejet d'une décision sur une tentative terminée.
 */
@SpringBootTest(classes = ChallengeITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class ChallengeAttemptIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("skillforge")
                    .withInitScript("db/challenge-it.sql");

    @DynamicPropertySource
    static void r2dbcProps(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
                + POSTGRES.getHost() + ":" + POSTGRES.getFirstMappedPort()
                + "/" + POSTGRES.getDatabaseName());
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
    }

    @Autowired
    ImportScenarioUseCase importScenario;
    @Autowired
    StartAttemptUseCase startAttempt;
    @Autowired
    SubmitDecisionUseCase submitDecision;
    @Autowired
    GetAttemptUseCase getAttempt;
    @Autowired
    CapturingChallengeEventListener capturedEvents;

    @Test
    void cycle_complet_demarrer_decider_rapport_puis_relecture() {
        UUID node = UUID.randomUUID();
        importScenario.importScenario(ScenarioFixtures.valid("attempt-it", node)).block();
        UUID learner = UUID.randomUUID();

        AttemptView started = startAttempt.start(learner, node).block();
        assertNotNull(started);
        assertEquals(AttemptStatus.IN_PROGRESS, started.status());
        assertNotNull(started.node());
        assertEquals("n1", started.node().id());
        assertEquals(1, started.node().choices().size());
        assertEquals("c1", started.node().choices().get(0).id());

        UUID attemptId = started.attemptId();
        AttemptView done = submitDecision.submit(learner, attemptId, "c1").block();
        assertNotNull(done);
        assertEquals(AttemptStatus.COMPLETED, done.status());
        assertNotNull(done.result());
        assertTrue(done.result().passed());
        assertEquals(100, done.result().globalScore());

        AttemptView reread = getAttempt.get(learner, attemptId).block();
        assertEquals(AttemptStatus.COMPLETED, reread.status());
        assertEquals(100, reread.result().globalScore());

        // La réussite émet un FinalChallengePassedEvent (consommé par apprentissage pour le gating).
        assertTrue(capturedEvents.events().stream().anyMatch(
                e -> e.nodeId().equals(node) && e.learnerId().equals(learner)));
    }

    @Test
    void une_tentative_appartient_a_son_apprenant() {
        UUID node = UUID.randomUUID();
        importScenario.importScenario(ScenarioFixtures.valid("attempt-owner", node)).block();
        UUID learner = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        AttemptView started = startAttempt.start(learner, node).block();

        assertThrows(NotFoundException.class,
                () -> getAttempt.get(other, started.attemptId()).block());
    }

    @Test
    void decider_sur_une_tentative_terminee_est_rejete() {
        UUID node = UUID.randomUUID();
        importScenario.importScenario(ScenarioFixtures.valid("attempt-closed", node)).block();
        UUID learner = UUID.randomUUID();

        AttemptView started = startAttempt.start(learner, node).block();
        submitDecision.submit(learner, started.attemptId(), "c1").block();

        assertThrows(BusinessRuleException.class,
                () -> submitDecision.submit(learner, started.attemptId(), "c1").block());
    }
}
