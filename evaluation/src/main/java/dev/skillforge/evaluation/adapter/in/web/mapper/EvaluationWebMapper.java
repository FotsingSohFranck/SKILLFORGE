package dev.skillforge.evaluation.adapter.in.web.mapper;

import dev.skillforge.evaluation.adapter.in.web.dto.AttemptResultDto;
import dev.skillforge.evaluation.adapter.in.web.dto.ChallengeDto;
import dev.skillforge.evaluation.adapter.in.web.dto.OptionDto;
import dev.skillforge.evaluation.adapter.in.web.dto.QuestionDto;
import dev.skillforge.evaluation.adapter.in.web.dto.QuizDto;
import dev.skillforge.evaluation.adapter.in.web.dto.SubmissionResultDto;
import dev.skillforge.evaluation.domain.model.AnswerResult;
import dev.skillforge.evaluation.domain.model.AttemptResult;
import dev.skillforge.evaluation.domain.model.CodeChallenge;
import dev.skillforge.evaluation.domain.model.OptionId;
import dev.skillforge.evaluation.domain.model.Quiz;
import dev.skillforge.evaluation.domain.model.SubmissionResult;

import java.util.UUID;

/** Traduction domaine → DTO web. La vue « passage » masque bonnes réponses et justifications. */
public final class EvaluationWebMapper {

    private EvaluationWebMapper() {
    }

    public static QuizDto toTakingDto(Quiz quiz) {
        return new QuizDto(
                quiz.id().value(),
                quiz.title(),
                quiz.description(),
                quiz.questions().stream().map(q -> new QuestionDto(
                        q.id().value(),
                        q.type().name(),
                        q.prompt(),
                        q.position(),
                        q.options().stream()
                                .map(o -> new OptionDto(o.id().value(), o.label(), o.position()))
                                .toList()
                )).toList());
    }

    public static AttemptResultDto toResultDto(AttemptResult result) {
        return new AttemptResultDto(
                result.quizId().value(),
                result.score(),
                result.maxScore(),
                result.answers().stream().map(EvaluationWebMapper::toAnswerDto).toList());
    }

    private static AttemptResultDto.AnswerResultDto toAnswerDto(AnswerResult a) {
        return new AttemptResultDto.AnswerResultDto(
                a.questionId().value(),
                unwrap(a.selectedOptionId()),
                a.correct(),
                unwrap(a.correctOptionId()),
                a.justification());
    }

    private static UUID unwrap(OptionId id) {
        return id == null ? null : id.value();
    }

    public static ChallengeDto toDto(CodeChallenge c) {
        return new ChallengeDto(c.id().value(), c.title(), c.statement(), c.skeleton(), c.language());
    }

    public static SubmissionResultDto toDto(SubmissionResult r) {
        return new SubmissionResultDto(r.passed(), r.matched(), r.missing(), r.solution());
    }

    public static dev.skillforge.evaluation.adapter.in.web.dto.ExamDto toTakingDto(
            dev.skillforge.evaluation.domain.model.MockExam exam) {
        return new dev.skillforge.evaluation.adapter.in.web.dto.ExamDto(
                exam.id().value(), exam.certificationId(), exam.title(), exam.format(), exam.durationMinutes(),
                exam.items().stream().map(item -> new dev.skillforge.evaluation.adapter.in.web.dto.ExamItemDto(
                        item.id().value(),
                        item.domainId().value(),
                        item.kind().name(),
                        item.prompt(),
                        item.points(),
                        item.position(),
                        item.options().stream()
                                .map(o -> new OptionDto(o.id().value(), o.label(), o.position()))
                                .toList()
                )).toList());
    }

    public static dev.skillforge.evaluation.adapter.in.web.dto.ExamAttemptDto toDto(
            dev.skillforge.evaluation.domain.model.ExamAttempt a) {
        return new dev.skillforge.evaluation.adapter.in.web.dto.ExamAttemptDto(
                a.id().value(), a.examId().value(), a.startedAt(), a.deadlineAt(), a.submittedAt(),
                a.totalScore(), a.maxScore(), a.passed(), a.status());
    }

    public static dev.skillforge.evaluation.adapter.in.web.dto.DomainResultDto toDto(
            dev.skillforge.evaluation.domain.model.DomainResult d) {
        return new dev.skillforge.evaluation.adapter.in.web.dto.DomainResultDto(
                d.domainId().value(), d.score(), d.maxScore(), d.weak());
    }

    public static dev.skillforge.evaluation.adapter.in.web.dto.ExamResultDto toDto(
            dev.skillforge.evaluation.domain.model.ExamResult r) {
        return new dev.skillforge.evaluation.adapter.in.web.dto.ExamResultDto(
                r.attemptId().value(), r.totalScore(), r.maxScore(), r.passed(), r.expired(),
                r.domains().stream().map(EvaluationWebMapper::toDto).toList());
    }
}
