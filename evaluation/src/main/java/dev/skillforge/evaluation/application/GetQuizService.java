package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.evaluation.domain.model.Quiz;
import dev.skillforge.evaluation.domain.model.QuizId;
import dev.skillforge.evaluation.domain.port.in.GetQuizUseCase;
import dev.skillforge.evaluation.domain.port.out.QuizRepository;
import reactor.core.publisher.Mono;

public class GetQuizService implements GetQuizUseCase {

    private final QuizRepository quizzes;

    public GetQuizService(QuizRepository quizzes) {
        this.quizzes = quizzes;
    }

    @Override
    public Mono<Quiz> get(QuizId quiz) {
        return quizzes.findById(quiz)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "QUIZ_NOT_FOUND", "Quiz introuvable : " + quiz.value())));
    }
}
