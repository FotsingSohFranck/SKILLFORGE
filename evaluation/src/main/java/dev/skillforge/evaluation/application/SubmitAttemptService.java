package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.evaluation.domain.model.AnswerResult;
import dev.skillforge.evaluation.domain.model.AttemptResult;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.model.Option;
import dev.skillforge.evaluation.domain.model.OptionId;
import dev.skillforge.evaluation.domain.model.Question;
import dev.skillforge.evaluation.domain.model.QuestionId;
import dev.skillforge.evaluation.domain.model.Quiz;
import dev.skillforge.evaluation.domain.model.QuizId;
import dev.skillforge.evaluation.domain.port.in.SubmitAttemptUseCase;
import dev.skillforge.evaluation.domain.port.in.SubmittedAnswer;
import dev.skillforge.evaluation.domain.port.out.AttemptStore;
import dev.skillforge.evaluation.domain.port.out.QuizRepository;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Corrige une tentative (une option correcte par question), persiste et renvoie le détail. */
public class SubmitAttemptService implements SubmitAttemptUseCase {

    private final QuizRepository quizzes;
    private final AttemptStore attempts;

    public SubmitAttemptService(QuizRepository quizzes, AttemptStore attempts) {
        this.quizzes = quizzes;
        this.attempts = attempts;
    }

    @Override
    public Mono<AttemptResult> submit(LearnerId learner, QuizId quiz, List<SubmittedAnswer> answers) {
        return quizzes.findById(quiz)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "QUIZ_NOT_FOUND", "Quiz introuvable : " + quiz.value())))
                .flatMap(loaded -> {
                    AttemptResult result = grade(loaded, answers);
                    return attempts.save(learner, quiz, result.score(), result.maxScore(), result.answers())
                            .thenReturn(result);
                });
    }

    private AttemptResult grade(Quiz quiz, List<SubmittedAnswer> answers) {
        Map<QuestionId, OptionId> selected = new HashMap<>();
        for (SubmittedAnswer answer : answers == null ? List.<SubmittedAnswer>of() : answers) {
            selected.putIfAbsent(answer.questionId(), answer.optionId());
        }
        List<AnswerResult> results = new ArrayList<>();
        int score = 0;
        for (Question question : quiz.questions()) {
            OptionId chosen = selected.get(question.id());
            Option correct = question.correctOption();
            OptionId correctId = correct == null ? null : correct.id();
            boolean isCorrect = chosen != null && correctId != null
                    && chosen.value().equals(correctId.value());
            if (isCorrect) {
                score++;
            }
            results.add(new AnswerResult(question.id(), chosen, isCorrect, correctId, question.justification()));
        }
        return new AttemptResult(quiz.id(), score, quiz.questions().size(), results);
    }
}
