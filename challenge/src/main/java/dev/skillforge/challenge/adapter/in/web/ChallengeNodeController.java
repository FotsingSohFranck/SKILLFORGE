package dev.skillforge.challenge.adapter.in.web;

import dev.skillforge.challenge.domain.model.ChallengeInfo;
import dev.skillforge.challenge.domain.port.in.GetChallengeInfoUseCase;
import dev.skillforge.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Entrée apprenant du Final Challenge d'un nœud : disponibilité + présentation (briefing/objectif),
 * sans aucune donnée de correction. Accès authentifié (tout apprenant).
 */
@RestController
@RequestMapping("/api/v1/final-challenges/nodes")
public class ChallengeNodeController {

    private final GetChallengeInfoUseCase info;

    public ChallengeNodeController(GetChallengeInfoUseCase info) {
        this.info = info;
    }

    @GetMapping("/{nodeId}")
    public Mono<ApiResponse<ChallengeInfo>> byNode(@PathVariable UUID nodeId) {
        return info.byNode(nodeId).map(ApiResponse::ok);
    }
}
