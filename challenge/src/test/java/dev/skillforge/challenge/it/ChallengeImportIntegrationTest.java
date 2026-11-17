package dev.skillforge.challenge.it;

import dev.skillforge.challenge.ScenarioFixtures;
import dev.skillforge.challenge.domain.model.ScenarioDefinition;
import dev.skillforge.challenge.domain.model.ScenarioRef;
import dev.skillforge.challenge.domain.port.in.GetScenarioUseCase;
import dev.skillforge.challenge.domain.port.in.ImportScenarioUseCase;
import dev.skillforge.common.error.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test d'intégration du contexte {@code challenge} (SF-92a-1) contre PostgreSQL 18 (Testcontainers) :
 * import d'un scénario → version publiée immuable ; réimport → nouvelle version ; relecture par nœud
 * (round-trip {@code jsonb} fidèle). Valide la chaîne authoring sans le moteur d'exécution (SF-92a-2).
 */
@SpringBootTest(classes = ChallengeITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class ChallengeImportIntegrationTest {

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
    GetScenarioUseCase getScenario;

    @Test
    void import_publie_une_version_et_relit_la_definition_a_l_identique() {
        UUID node = UUID.randomUUID();
        ScenarioDefinition original = ScenarioFixtures.valid("round-trip", node);

        ScenarioRef ref = importScenario.importScenario(original).block();
        assertNotNull(ref);
        assertNotNull(ref.scenarioId());
        assertEquals(1, ref.version(), "le premier import doit créer la version 1");

        ScenarioDefinition read = getScenario.latestPublishedByNode(node).block();
        assertEquals(original, read, "la définition relue doit être identique (round-trip jsonb)");
    }

    @Test
    void reimport_cree_une_nouvelle_version_immuable() {
        UUID node = UUID.randomUUID();
        ScenarioDefinition v1 = ScenarioFixtures.valid("versionne", node);

        ScenarioRef first = importScenario.importScenario(v1).block();
        ScenarioRef second = importScenario.importScenario(v1).block();

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(1, first.version());
        assertEquals(2, second.version(), "un réimport de la même clé crée la version suivante");
        assertEquals(first.scenarioId(), second.scenarioId(), "même scénario, nouvelle version");
    }

    @Test
    void definition_invalide_rejetee_sans_persistance() {
        UUID node = UUID.randomUUID();
        // Somme des pondérations != 100.
        ScenarioDefinition invalid = ScenarioFixtures.def("invalide", node,
                List.of("a", "b"), Map.of("a", 50, "b", 40), 80, 60, "n1",
                List.of(ScenarioFixtures.choiceNode("n1", "n2"), ScenarioFixtures.terminal("n2")));

        assertThrows(BusinessRuleException.class,
                () -> importScenario.importScenario(invalid).block());
        // Rien n'a été publié pour ce nœud.
        assertEquals(null, getScenario.latestPublishedByNode(node).block());
    }
}
