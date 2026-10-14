package dev.skillforge.evaluation.it;

import dev.skillforge.evaluation.domain.model.ExamAttempt;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.model.MockExam;
import dev.skillforge.evaluation.domain.port.in.ExamImport;
import dev.skillforge.evaluation.domain.port.in.ExamItemAnswer;
import dev.skillforge.evaluation.domain.port.in.GetExamHistoryUseCase;
import dev.skillforge.evaluation.domain.port.in.GetExamUseCase;
import dev.skillforge.evaluation.domain.port.in.GetRevisionUseCase;
import dev.skillforge.evaluation.domain.port.in.IngestExamUseCase;
import dev.skillforge.evaluation.domain.port.in.ListCertificationExamsUseCase;
import dev.skillforge.evaluation.domain.port.in.StartExamUseCase;
import dev.skillforge.evaluation.domain.port.in.SubmitExamUseCase;
import dev.skillforge.evaluation.domain.model.ExamItem;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test d'intégration des examens blancs : ingestion, démarrage (deadline), soumission avec
 * score par domaine + points faibles, historique et révision ciblée contre PostgreSQL 18.
 */
@SpringBootTest(classes = EvaluationITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class ExamIntegrationTest {

    private static final UUID EXAM = UUID.fromString("019c0000-0000-7000-8000-0000000000e0");
    private static final UUID CERT = UUID.fromString("019c0000-0000-7000-8000-0000000000f0");
    private static final UUID D1 = UUID.fromString("019c0000-0000-7000-8000-0000000000d1");
    private static final UUID D2 = UUID.fromString("019c0000-0000-7000-8000-0000000000d2");

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("skillforge")
                    .withInitScript("db/evaluation-it.sql");

    @DynamicPropertySource
    static void r2dbcProps(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
                + POSTGRES.getHost() + ":" + POSTGRES.getFirstMappedPort()
                + "/" + POSTGRES.getDatabaseName());
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
    }

    @Autowired IngestExamUseCase ingest;
    @Autowired GetExamUseCase getExam;
    @Autowired StartExamUseCase start;
    @Autowired SubmitExamUseCase submit;
    @Autowired GetExamHistoryUseCase history;
    @Autowired GetRevisionUseCase revision;
    @Autowired ListCertificationExamsUseCase listExams;

    @Test
    void flux_complet_examen_avec_score_par_domaine_et_revision() {
        LearnerId learner = new LearnerId(UUID.randomUUID());

        // 1) Ingestion : examen 2 domaines, 3 tâches (D1: 2, D2: 1).
        ingest.ingest(new ExamImport(EXAM, CERT, "CKAD blanc", "PRACTICAL", 120, 66.0, List.of(
                new ExamImport.ItemImport(D1, "TASK", null, "Créer un Pod", "kubectl run", 1.0, 0),
                new ExamImport.ItemImport(D1, "TASK", null, "Créer un Service", "kubectl expose", 1.0, 1),
                new ExamImport.ItemImport(D2, "TASK", null, "Déployer", "kubectl apply", 1.0, 2)))).block();

        // 2) Démarrage : la deadline est bien positionnée (minuteur).
        ExamAttempt attempt = start.start(learner, new ExamId(EXAM)).block();
        assertTrue(attempt.deadlineAt().isAfter(attempt.startedAt()), "deadline > début");

        // Récupère les ids d'items via l'examen chargé.
        MockExam exam = getExam.get(new ExamId(EXAM)).block();
        List<ExamItem> items = exam.items();

        // 3) Soumission : D1 -> 1/2 (faible), D2 -> 1/1.
        var result = submit.submit(learner, attempt.id(), List.of(
                new ExamItemAnswer(items.get(0).id(), null, true),   // D1 tâche 1 : fait
                new ExamItemAnswer(items.get(1).id(), null, false),  // D1 tâche 2 : non fait
                new ExamItemAnswer(items.get(2).id(), null, true)    // D2 : fait
        )).block();

        assertEquals(2.0, result.totalScore(), 0.001);
        assertEquals(3.0, result.maxScore(), 0.001);
        assertTrue(result.passed());
        assertEquals(2, result.domains().size());

        // 4) Historique : une tentative.
        assertEquals(1, history.history(learner, new ExamId(EXAM)).collectList().block().size());

        // 5) Révision ciblée : le domaine D1 (faible) ressort.
        var weak = revision.weakDomains(learner, new ExamId(EXAM)).collectList().block();
        assertEquals(1, weak.size());
        assertEquals(D1, weak.get(0).domainId().value());

        // 6) Découverte : l'examen est listé pour sa certification.
        var byCert = listExams.list(CERT).collectList().block();
        assertEquals(1, byCert.size());
        assertEquals(EXAM, byCert.get(0).id().value());
        assertEquals("CKAD blanc", byCert.get(0).title());
    }
}
