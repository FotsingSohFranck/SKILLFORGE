package dev.skillforge.apprentissage.domain.port.out;

import reactor.core.publisher.Mono;

/**
 * Port sortant : publication des événements d'intégration (définis dans {@code common.event}),
 * consommés par gamification. Le type est neutre ({@code Object}) pour ne pas coupler le
 * contexte aux consommateurs.
 */
public interface LearningEventPublisher {
    Mono<Void> publish(Object event);
}
