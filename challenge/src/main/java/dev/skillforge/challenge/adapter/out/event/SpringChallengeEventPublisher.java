package dev.skillforge.challenge.adapter.out.event;

import dev.skillforge.challenge.domain.port.out.ChallengeEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/** Publie les événements d'intégration du contexte challenge sur le bus Spring (in-process). */
@Component
public class SpringChallengeEventPublisher implements ChallengeEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringChallengeEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public Mono<Void> publish(Object event) {
        return Mono.fromRunnable(() -> publisher.publishEvent(event));
    }
}
