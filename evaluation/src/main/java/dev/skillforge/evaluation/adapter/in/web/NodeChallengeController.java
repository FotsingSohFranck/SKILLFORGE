package dev.skillforge.evaluation.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.evaluation.adapter.in.web.dto.ChallengeSummaryDto;
import dev.skillforge.evaluation.domain.port.in.ListNodeChallengesUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/** Découverte des défis de code rattachés à un nœud. */
@RestController
@RequestMapping("/api/v1/nodes")
public class NodeChallengeController {

    private final ListNodeChallengesUseCase listNodeChallenges;

    public NodeChallengeController(ListNodeChallengesUseCase listNodeChallenges) {
        this.listNodeChallenges = listNodeChallenges;
    }

    @GetMapping("/{nodeId}/challenges")
    public Mono<ApiResponse<List<ChallengeSummaryDto>>> byNode(@PathVariable UUID nodeId) {
        return listNodeChallenges.list(nodeId)
                .map(c -> new ChallengeSummaryDto(c.id().value(), c.title(), c.language()))
                .collectList()
                .map(ApiResponse::ok);
    }
}
