package dev.skillforge.evaluation.application;

import dev.skillforge.evaluation.domain.model.ChallengeSummary;
import dev.skillforge.evaluation.domain.port.in.ListNodeChallengesUseCase;
import dev.skillforge.evaluation.domain.port.out.ChallengeRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/** Liste les défis de code d'un nœud. */
public class ListNodeChallengesService implements ListNodeChallengesUseCase {

    private final ChallengeRepository challenges;

    public ListNodeChallengesService(ChallengeRepository challenges) {
        this.challenges = challenges;
    }

    @Override
    public Flux<ChallengeSummary> list(UUID nodeId) {
        return challenges.findByNode(nodeId);
    }
}
