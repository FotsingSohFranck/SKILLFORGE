package dev.skillforge.challenge.domain.port.out;

import reactor.core.publisher.Mono;

/** Port sortant : publication des événements d'intégration du contexte challenge. */
public interface ChallengeEventPublisher {

    Mono<Void> publish(Object event);
}
