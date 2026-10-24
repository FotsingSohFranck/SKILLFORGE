package dev.skillforge.lab.adapter.out.event;

import dev.skillforge.lab.domain.port.out.LabEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/** Publie les événements d'intégration du contexte lab sur le bus Spring (in-process). */
@Component
public class SpringLabEventPublisher implements LabEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringLabEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public Mono<Void> publish(Object event) {
        return Mono.fromRunnable(() -> publisher.publishEvent(event));
    }
}
