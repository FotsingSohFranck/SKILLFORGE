package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.evaluation.domain.model.DomainResult;
import dev.skillforge.evaluation.domain.model.ExamAttempt;
import dev.skillforge.evaluation.domain.model.ExamAttemptId;
import dev.skillforge.evaluation.domain.model.ExamDomainId;
import dev.skillforge.evaluation.domain.model.ExamItem;
import dev.skillforge.evaluation.domain.model.ExamItemKind;
import dev.skillforge.evaluation.domain.model.ExamResult;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.model.MockExam;
import dev.skillforge.evaluation.domain.model.Option;
import dev.skillforge.common.event.ExamPassedEvent;
import dev.skillforge.evaluation.domain.port.in.ExamItemAnswer;
import dev.skillforge.evaluation.domain.port.in.SubmitExamUseCase;
import dev.skillforge.evaluation.domain.port.out.EvaluationEventPublisher;
import dev.skillforge.evaluation.domain.port.out.ExamAttemptStore;
import dev.skillforge.evaluation.domain.port.out.ExamRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Notation d'un examen : QCM auto-corrigés (option correcte), tâches auto-évaluées
 * ({@code completed}). Agrège le score <b>par domaine</b>, détecte les points faibles
 * (&lt; 66 %) et détermine la réussite (seuil de la certification, défaut 66 %).
 */
public class SubmitExamService implements SubmitExamUseCase {

    private static final double WEAK_THRESHOLD = 66.0;

    private final ExamRepository exams;
    private final ExamAttemptStore attempts;
    private final EvaluationEventPublisher events;

    public SubmitExamService(ExamRepository exams, ExamAttemptStore attempts, EvaluationEventPublisher events) {
        this.exams = exams;
        this.attempts = attempts;
        this.events = events;
    }

    @Override
    public Mono<ExamResult> submit(LearnerId learner, ExamAttemptId attempt, List<ExamItemAnswer> answers) {
        return attempts.findById(attempt)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "EXAM_ATTEMPT_NOT_FOUND", "Tentative introuvable : " + attempt.value())))
                .flatMap(loaded -> exams.findById(loaded.examId())
                        .switchIfEmpty(Mono.error(new NotFoundException(
                                "EXAM_NOT_FOUND", "Examen introuvable : " + loaded.examId().value())))
                        .flatMap(exam -> {
                            ExamResult result = grade(loaded, exam, answers, Instant.now());
                            String status = result.expired() ? "EXPIRED" : "SUBMITTED";
                            return attempts.complete(attempt, Instant.now(), result.totalScore(),
                                            result.maxScore(), result.passed(), status, result.domains())
                                    .then(publishIfPassed(result, learner, exam.id().value(), exam.certificationId()))
                                    .thenReturn(result);
                        }));
    }

    private Mono<Void> publishIfPassed(ExamResult result, LearnerId learner, java.util.UUID examId,
                                       java.util.UUID certificationId) {
        if (result.passed()) {
            return events.publish(new ExamPassedEvent(learner.value(), examId, certificationId));
        }
        return Mono.empty();
    }

    private ExamResult grade(ExamAttempt attempt, MockExam exam, List<ExamItemAnswer> answers, Instant now) {
        Map<java.util.UUID, ExamItemAnswer> byItem = new HashMap<>();
        for (ExamItemAnswer answer : answers == null ? List.<ExamItemAnswer>of() : answers) {
            byItem.putIfAbsent(answer.itemId().value(), answer);
        }
        Map<ExamDomainId, double[]> perDomain = new LinkedHashMap<>();
        for (ExamItem item : exam.items()) {
            double awarded = award(item, byItem.get(item.id().value()));
            double[] acc = perDomain.computeIfAbsent(item.domainId(), k -> new double[2]);
            acc[0] += awarded;
            acc[1] += item.points();
        }
        double total = 0;
        double maxTotal = 0;
        List<DomainResult> domains = new ArrayList<>();
        for (Map.Entry<ExamDomainId, double[]> entry : perDomain.entrySet()) {
            double score = entry.getValue()[0];
            double max = entry.getValue()[1];
            total += score;
            maxTotal += max;
            boolean weak = max > 0 && (score / max * 100.0) < WEAK_THRESHOLD;
            domains.add(new DomainResult(entry.getKey(), score, max, weak));
        }
        double threshold = exam.passingScore() != null ? exam.passingScore() : WEAK_THRESHOLD;
        boolean passed = maxTotal > 0 && (total / maxTotal * 100.0) >= threshold;
        boolean expired = attempt.deadlineAt() != null && now.isAfter(attempt.deadlineAt());
        return new ExamResult(attempt.id(), total, maxTotal, passed, expired, domains);
    }

    private double award(ExamItem item, ExamItemAnswer answer) {
        if (answer == null) {
            return 0;
        }
        if (item.kind() == ExamItemKind.MCQ) {
            Option correct = item.correctOption();
            boolean ok = answer.selectedOptionId() != null && correct != null
                    && answer.selectedOptionId().value().equals(correct.id().value());
            return ok ? item.points() : 0;
        }
        return Boolean.TRUE.equals(answer.completed()) ? item.points() : 0;
    }
}
