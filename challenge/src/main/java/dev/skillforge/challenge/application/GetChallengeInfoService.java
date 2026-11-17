package dev.skillforge.challenge.application;

import dev.skillforge.challenge.domain.model.ChallengeInfo;
import dev.skillforge.challenge.domain.port.in.GetChallengeInfoUseCase;
import dev.skillforge.challenge.domain.port.out.ScenarioStore;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Expose la disponibilité du Final Challenge d'un nœud sans révéler la définition (correction). */
public class GetChallengeInfoService implements GetChallengeInfoUseCase {

    private final ScenarioStore store;

    public GetChallengeInfoService(ScenarioStore store) {
        this.store = store;
    }

    @Override
    public Mono<ChallengeInfo> byNode(UUID nodeId) {
        return store.findLatestPublishedByNode(nodeId)
                .map(ChallengeInfo::of)
                .defaultIfEmpty(ChallengeInfo.unavailable());
    }
}
