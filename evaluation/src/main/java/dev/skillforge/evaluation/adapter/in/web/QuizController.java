package dev.skillforge.evaluation.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.evaluation.adapter.in.web.dto.AttemptRequest;
import dev.skillforge.evaluation.adapter.in.web.dto.AttemptResultDto;
import dev.skillforge.evaluation.adapter.in.web.dto.QuizDto;
import dev.skillforge.evaluation.adapter.in.web.mapper.EvaluationWebMapper;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.model.OptionId;
import dev.skillforge.evaluation.domain.model.QuestionId;
import dev.skillforge.evaluation.domain.model.QuizId;
import dev.skillforge.evaluation.domain.port.in.GetQuizUseCase;
import dev.skillforge.evaluation.domain.port.in.SubmitAttemptUseCase;
import dev.skillforge.evaluation.domain.port.in.SubmittedAnswer;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** Passage de quiz : lecture (sans réponses) et soumission (correction immédiate). */
@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final GetQuizUseCase getQuiz;
    private final SubmitAttemptUseCase submitAttempt;

    public QuizController(GetQuizUseCase getQuiz, SubmitAttemptUseCase submitAttempt) {
        this.getQuiz = getQuiz;
        this.submitAttempt = submitAttempt;
    }

    @GetMapping("/{quizId}")
    public Mono<ApiResponse<QuizDto>> get(@PathVariable UUID quizId) {
        return getQuiz.get(new QuizId(quizId))
                .map(EvaluationWebMapper::toTakingDto)
                .map(ApiResponse::ok);
    }

    @PostMapping("/{quizId}/attempts")
    public Mono<ApiResponse<AttemptResultDto>> submit(
            Principal principal, @PathVariable UUID quizId, @Valid @RequestBody AttemptRequest request) {
        List<SubmittedAnswer> answers = request.answers().stream()
                .map(a -> new SubmittedAnswer(new QuestionId(a.questionId()), new OptionId(a.optionId())))
                .toList();
        return submitAttempt.submit(learner(principal), new QuizId(quizId), answers)
                .map(EvaluationWebMapper::toResultDto)
                .map(ApiResponse::ok);
    }

    private static LearnerId learner(Principal principal) {
        return new LearnerId(UUID.fromString(principal.getName()));
    }
}
