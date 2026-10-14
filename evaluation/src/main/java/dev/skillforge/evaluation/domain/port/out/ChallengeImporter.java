package dev.skillforge.evaluation.domain.port.out;

import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.port.in.ChallengeImport;
import reactor.core.publisher.Mono;

/** Port sortant : persistance idempotente d'un défi (upsert par id). */
public interface ChallengeImporter {
    Mono<ChallengeId> importChallenge(ChallengeImport command);
}
