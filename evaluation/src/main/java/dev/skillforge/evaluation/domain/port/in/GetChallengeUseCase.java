package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.model.CodeChallenge;
import reactor.core.publisher.Mono;

/** Obtenir un défi (404 si absent). Le web masque solution et points clés avant soumission. */
public interface GetChallengeUseCase {
    Mono<CodeChallenge> get(ChallengeId challenge);
}
