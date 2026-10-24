package dev.skillforge.lab.domain.port.out;

import reactor.core.publisher.Mono;

/** Port sortant : publication des événements d'intégration du contexte lab (cf. {@code common.event}). */
public interface LabEventPublisher {
    Mono<Void> publish(Object event);
}
