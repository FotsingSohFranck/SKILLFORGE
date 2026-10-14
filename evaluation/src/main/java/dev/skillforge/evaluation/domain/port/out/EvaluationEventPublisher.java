package dev.skillforge.evaluation.domain.port.out;

import reactor.core.publisher.Mono;

/** Port sortant : publication des événements d'intégration (définis dans {@code common.event}). */
public interface EvaluationEventPublisher {
    Mono<Void> publish(Object event);
}
