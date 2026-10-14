package dev.skillforge.evaluation.adapter.out.event;

import dev.skillforge.evaluation.domain.port.out.EvaluationEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/** Publie les événements d'intégration sur le bus Spring (in-process). */
@Component
public class SpringEvaluationEventPublisher implements EvaluationEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringEvaluationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public Mono<Void> publish(Object event) {
        return Mono.fromRunnable(() -> publisher.publishEvent(event));
    }
}
