package dev.skillforge.evaluation.application;

import dev.skillforge.evaluation.domain.model.AnswerResult;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.model.Option;
import dev.skillforge.evaluation.domain.model.OptionId;
import dev.skillforge.evaluation.domain.model.Question;
import dev.skillforge.evaluation.domain.model.QuestionId;
import dev.skillforge.evaluation.domain.model.QuestionType;
import dev.skillforge.evaluation.domain.model.Quiz;
import dev.skillforge.evaluation.domain.model.QuizId;
import dev.skillforge.evaluation.domain.port.in.SubmittedAnswer;
import dev.skillforge.evaluation.domain.port.out.AttemptStore;
import dev.skillforge.evaluation.domain.port.out.QuizRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubmitAttemptServiceTest {

    private final QuizId quizId = new QuizId(UUID.randomUUID());
    private final OptionId q1Correct = new OptionId(UUID.randomUUID());
    private final OptionId q1Wrong = new OptionId(UUID.randomUUID());
    private final OptionId q2Correct = new OptionId(UUID.randomUUID());
    private final OptionId q2Wrong = new OptionId(UUID.randomUUID());
    private final QuestionId q1 = new QuestionId(UUID.randomUUID());
    private final QuestionId q2 = new QuestionId(UUID.randomUUID());

    private Quiz quiz() {
        Question question1 = new Question(q1, QuestionType.MCQ, "Q1", "car A est correct", 0,
                List.of(new Option(q1Correct, "A", 0, true), new Option(q1Wrong, "B", 1, false)));
        Question question2 = new Question(q2, QuestionType.TRUE_FALSE, "Q2", "car vrai", 1,
                List.of(new Option(q2Correct, "Vrai", 0, true), new Option(q2Wrong, "Faux", 1, false)));
        return new Quiz(quizId, "Quiz", null, List.of(question1, question2));
    }

    private final int[] savedScore = {-1, -1};
    private final AttemptStore attempts = (learner, quiz, score, max, answers) -> {
        savedScore[0] = score;
        savedScore[1] = max;
        return Mono.empty();
    };
    private final QuizRepository quizzes = id -> Mono.just(quiz());

    @Test
    void corrige_une_reponse_bonne_et_une_mauvaise() {
        var service = new SubmitAttemptService(quizzes, attempts);
        List<SubmittedAnswer> answers = List.of(
                new SubmittedAnswer(q1, q1Correct),   // bon
                new SubmittedAnswer(q2, q2Wrong));    // mauvais

        StepVerifier.create(service.submit(new LearnerId(UUID.randomUUID()), quizId, answers))
                .assertNext(result -> {
                    assertEquals(1, result.score());
                    assertEquals(2, result.maxScore());
                    AnswerResult a1 = result.answers().get(0);
                    assertTrue(a1.correct());
                    assertEquals(q1Correct.value(), a1.correctOptionId().value());
                    assertEquals("car A est correct", a1.justification());
                    assertFalse(result.answers().get(1).correct());
                })
                .verifyComplete();
        assertEquals(1, savedScore[0], "score persisté");
        assertEquals(2, savedScore[1], "max persisté");
    }

    @Test
    void question_non_repondue_compte_comme_fausse() {
        var service = new SubmitAttemptService(quizzes, attempts);
        StepVerifier.create(service.submit(new LearnerId(UUID.randomUUID()), quizId,
                        List.of(new SubmittedAnswer(q1, q1Correct)))) // q2 non répondue
                .assertNext(result -> assertEquals(1, result.score()))
                .verifyComplete();
    }
}
