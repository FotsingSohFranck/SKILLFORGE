package dev.skillforge.challenge.domain.port.in;

import dev.skillforge.challenge.domain.model.AttemptView;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Démarre une tentative sur le scénario publié rattaché à un nœud. */
public interface StartAttemptUseCase {

    Mono<AttemptView> start(UUID learnerId, UUID nodeId);
}
