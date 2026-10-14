package dev.skillforge.apprentissage.adapter.out.event;

import dev.skillforge.apprentissage.domain.port.out.LearningEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Publie les événements d'intégration sur le bus Spring (in-process). Le contexte gamification
 * s'y abonne via {@code @EventListener}.
 */
@Component
public class SpringLearningEventPublisher implements LearningEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringLearningEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public Mono<Void> publish(Object event) {
        return Mono.fromRunnable(() -> publisher.publishEvent(event));
    }
}
