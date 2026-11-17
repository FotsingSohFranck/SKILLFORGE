package dev.skillforge.challenge.adapter.in.web;

import dev.skillforge.challenge.adapter.in.web.dto.AttemptDto;
import dev.skillforge.challenge.adapter.in.web.dto.StartAttemptRequest;
import dev.skillforge.challenge.adapter.in.web.dto.SubmitDecisionRequest;
import dev.skillforge.challenge.domain.port.in.GetAttemptUseCase;
import dev.skillforge.challenge.domain.port.in.StartAttemptUseCase;
import dev.skillforge.challenge.domain.port.in.SubmitDecisionUseCase;
import dev.skillforge.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.UUID;

/**
 * Tentatives de Final Challenge (apprenant authentifié) : démarrer une simulation, lire son état,
 * soumettre une décision. Le scoring fait foi côté serveur ; la réponse n'expose jamais la correction.
 */
@RestController
@RequestMapping("/api/v1/final-challenges/attempts")
public class AttemptController {

    private final StartAttemptUseCase startAttempt;
    private final SubmitDecisionUseCase submitDecision;
    private final GetAttemptUseCase getAttempt;

    public AttemptController(StartAttemptUseCase startAttempt, SubmitDecisionUseCase submitDecision,
                            GetAttemptUseCase getAttempt) {
        this.startAttempt = startAttempt;
        this.submitDecision = submitDecision;
        this.getAttempt = getAttempt;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<AttemptDto>>> start(
            Principal principal, @Valid @RequestBody StartAttemptRequest request) {
        return startAttempt.start(learner(principal), request.nodeId())
                .map(AttemptDto::from)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto)));
    }

    @GetMapping("/{attemptId}")
    public Mono<ApiResponse<AttemptDto>> get(Principal principal, @PathVariable UUID attemptId) {
        return getAttempt.get(learner(principal), attemptId)
                .map(AttemptDto::from)
                .map(ApiResponse::ok);
    }

    @PostMapping("/{attemptId}/decisions")
    public Mono<ApiResponse<AttemptDto>> decide(
            Principal principal, @PathVariable UUID attemptId, @Valid @RequestBody SubmitDecisionRequest request) {
        return submitDecision.submit(learner(principal), attemptId, request.choiceId())
                .map(AttemptDto::from)
                .map(ApiResponse::ok);
    }

    private static UUID learner(Principal principal) {
        return UUID.fromString(principal.getName());
    }
}
