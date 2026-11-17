package dev.skillforge.challenge.domain.port.in;

import dev.skillforge.challenge.domain.model.ChallengeInfo;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Disponibilité + présentation (sans correction) du Final Challenge d'un nœud, pour l'apprenant. */
public interface GetChallengeInfoUseCase {

    Mono<ChallengeInfo> byNode(UUID nodeId);
}
