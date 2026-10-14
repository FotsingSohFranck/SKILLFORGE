package dev.skillforge.evaluation.domain.port.out;

import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.model.ChallengeSummary;
import dev.skillforge.evaluation.domain.model.CodeChallenge;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Port sortant : lecture d'un défi de code complet (avec points clés et solution). */
public interface ChallengeRepository {
    Mono<CodeChallenge> findById(ChallengeId challenge);

    /** Résumés des défis rattachés à un nœud (référence souple vers catalogue.nodes). */
    Flux<ChallengeSummary> findByNode(UUID nodeId);
}
