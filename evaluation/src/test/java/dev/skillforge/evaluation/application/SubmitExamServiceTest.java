package dev.skillforge.evaluation.application;

import dev.skillforge.evaluation.domain.model.DomainResult;
import dev.skillforge.evaluation.domain.model.ExamAttempt;
import dev.skillforge.evaluation.domain.model.ExamAttemptId;
import dev.skillforge.evaluation.domain.model.ExamDomainId;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.ExamItem;
import dev.skillforge.evaluation.domain.model.ExamItemId;
import dev.skillforge.evaluation.domain.model.ExamItemKind;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.model.MockExam;
import dev.skillforge.evaluation.domain.port.in.ExamItemAnswer;
import dev.skillforge.evaluation.domain.port.out.ExamAttemptStore;
import dev.skillforge.evaluation.domain.port.out.ExamRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubmitExamServiceTest {

    private final ExamId examId = new ExamId(UUID.randomUUID());
    private final ExamAttemptId attemptId = new ExamAttemptId(UUID.randomUUID());
    private final ExamDomainId d1 = new ExamDomainId(UUID.randomUUID());
    private final ExamDomainId d2 = new ExamDomainId(UUID.randomUUID());
    private final ExamItemId i1 = new ExamItemId(UUID.randomUUID());
    private final ExamItemId i2 = new ExamItemId(UUID.randomUUID());
    private final ExamItemId i3 = new ExamItemId(UUID.randomUUID());

    private MockExam exam() {
        return new MockExam(examId, UUID.randomUUID(), "CKAD", "PRACTICAL", 120, 66.0, List.of(
                new ExamItem(i1, d1, ExamItemKind.TASK, "Tâche 1", 1, 0, List.of(), "sol"),
                new ExamItem(i2, d1, ExamItemKind.TASK, "Tâche 2", 1, 1, List.of(), "sol"),
                new ExamItem(i3, d2, ExamItemKind.TASK, "Tâche 3", 1, 2, List.of(), "sol")));
    }

    private final ExamAttempt attempt = new ExamAttempt(attemptId, examId,
            new LearnerId(UUID.randomUUID()), Instant.now(), Instant.now().plusSeconds(3600),
            null, 0, 0, null, "IN_PROGRESS");

    private List<DomainResult> savedDomains;
    private final ExamAttemptStore attempts = new ExamAttemptStore() {
        @Override
        public Mono<ExamAttempt> start(LearnerId l, ExamId e, Instant s, Instant d) {
            return Mono.just(attempt);
        }

        @Override
        public Mono<ExamAttempt> findById(ExamAttemptId a) {
            return Mono.just(attempt);
        }

        @Override
        public Mono<Void> complete(ExamAttemptId a, Instant sub, double total, double max,
                                   boolean passed, String status, List<DomainResult> domains) {
            savedDomains = domains;
            return Mono.empty();
        }

        @Override
        public Flux<ExamAttempt> history(LearnerId l, ExamId e) {
            return Flux.empty();
        }

        @Override
        public Mono<ExamAttempt> lastSubmitted(LearnerId l, ExamId e) {
            return Mono.empty();
        }

        @Override
        public Flux<DomainResult> domainResults(ExamAttemptId a) {
            return Flux.empty();
        }
    };
    private final ExamRepository exams = new ExamRepository() {
        @Override
        public Mono<dev.skillforge.evaluation.domain.model.MockExam> findById(
                dev.skillforge.evaluation.domain.model.ExamId id) {
            return Mono.just(exam());
        }

        @Override
        public Flux<dev.skillforge.evaluation.domain.model.ExamSummary> findByCertification(
                java.util.UUID certificationId) {
            return Flux.empty();
        }
    };
    private final dev.skillforge.evaluation.domain.port.out.EvaluationEventPublisher events =
            event -> Mono.empty();

    @Test
    void score_par_domaine_avec_points_faibles() {
        var service = new SubmitExamService(exams, attempts, events);
        // D1 : 1 tâche faite sur 2 (50 % -> faible) ; D2 : 1/1 (100 %).
        List<ExamItemAnswer> answers = List.of(
                new ExamItemAnswer(i1, null, true),
                new ExamItemAnswer(i2, null, false),
                new ExamItemAnswer(i3, null, true));

        StepVerifier.create(service.submit(attempt.learnerId(), attemptId, answers))
                .assertNext(result -> {
                    assertEquals(2.0, result.totalScore(), 0.001);
                    assertEquals(3.0, result.maxScore(), 0.001);
                    assertTrue(result.passed(), "2/3 = 66.6% >= 66% -> réussi");
                    DomainResult r1 = result.domains().stream()
                            .filter(d -> d.domainId().equals(d1)).findFirst().orElseThrow();
                    DomainResult r2 = result.domains().stream()
                            .filter(d -> d.domainId().equals(d2)).findFirst().orElseThrow();
                    assertTrue(r1.weak(), "domaine D1 faible (50%)");
                    assertTrue(!r2.weak(), "domaine D2 non faible (100%)");
                })
                .verifyComplete();
        assertEquals(2, savedDomains.size(), "résultats par domaine persistés");
    }
}
