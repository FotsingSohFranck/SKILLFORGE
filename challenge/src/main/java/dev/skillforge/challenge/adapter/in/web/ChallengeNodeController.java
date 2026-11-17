package dev.skillforge.challenge.adapter.in.web;

import dev.skillforge.challenge.adapter.in.web.dto.AttemptDto;
import dev.skillforge.challenge.domain.model.ChallengeInfo;
import dev.skillforge.challenge.domain.port.in.GetChallengeInfoUseCase;
import dev.skillforge.challenge.domain.port.in.ResumeAttemptUseCase;
import dev.skillforge.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.UUID;

/**
 * Entrée apprenant du Final Challenge d'un nœud : disponibilité + présentation (briefing/objectif,
 * sans correction) et reprise de la tentative en cours. Accès authentifié (tout apprenant).
 */
@RestController
@RequestMapping("/api/v1/final-challenges/nodes")
public class ChallengeNodeController {

    private final GetChallengeInfoUseCase info;
    private final ResumeAttemptUseCase resume;

    public ChallengeNodeController(GetChallengeInfoUseCase info, ResumeAttemptUseCase resume) {
        this.info = info;
        this.resume = resume;
    }

    @GetMapping("/{nodeId}")
    public Mono<ApiResponse<ChallengeInfo>> byNode(@PathVariable UUID nodeId) {
        return info.byNode(nodeId).map(ApiResponse::ok);
    }

    /** Tentative en cours de l'apprenant sur ce nœud (reprise), ou {@code data} null s'il n'y en a pas. */
    @GetMapping("/{nodeId}/current-attempt")
    public Mono<ApiResponse<AttemptDto>> currentAttempt(Principal principal, @PathVariable UUID nodeId) {
        return resume.currentForNode(UUID.fromString(principal.getName()), nodeId)
                .map(AttemptDto::from)
                .map(ApiResponse::ok)
                .defaultIfEmpty(ApiResponse.ok(null));
    }
}
