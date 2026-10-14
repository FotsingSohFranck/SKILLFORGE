package dev.skillforge.evaluation.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.evaluation.adapter.in.web.dto.ChallengeDto;
import dev.skillforge.evaluation.adapter.in.web.dto.SubmissionResultDto;
import dev.skillforge.evaluation.adapter.in.web.dto.SubmitCodeRequest;
import dev.skillforge.evaluation.adapter.in.web.mapper.EvaluationWebMapper;
import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.port.in.GetChallengeUseCase;
import dev.skillforge.evaluation.domain.port.in.SubmitChallengeUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Défis de code : lecture (sans solution) et soumission (validation par points clés). */
@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeController {

    private final GetChallengeUseCase getChallenge;
    private final SubmitChallengeUseCase submitChallenge;

    public ChallengeController(GetChallengeUseCase getChallenge, SubmitChallengeUseCase submitChallenge) {
        this.getChallenge = getChallenge;
        this.submitChallenge = submitChallenge;
    }

    @GetMapping("/{challengeId}")
    public Mono<ApiResponse<ChallengeDto>> get(@PathVariable UUID challengeId) {
        return getChallenge.get(new ChallengeId(challengeId))
                .map(EvaluationWebMapper::toDto)
                .map(ApiResponse::ok);
    }

    @PostMapping("/{challengeId}/submit")
    public Mono<ApiResponse<SubmissionResultDto>> submit(
            @PathVariable UUID challengeId, @Valid @RequestBody SubmitCodeRequest request) {
        return submitChallenge.submit(new ChallengeId(challengeId), request.code())
                .map(EvaluationWebMapper::toDto)
                .map(ApiResponse::ok);
    }
}
