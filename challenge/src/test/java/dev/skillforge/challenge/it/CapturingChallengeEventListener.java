package dev.skillforge.challenge.it;

import dev.skillforge.common.event.FinalChallengePassedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Capture les {@code FinalChallengePassedEvent} émis sur le bus Spring pendant les tests. */
@Component
public class CapturingChallengeEventListener {

    private final List<FinalChallengePassedEvent> events = new CopyOnWriteArrayList<>();

    @EventListener
    public void on(FinalChallengePassedEvent event) {
        events.add(event);
    }

    public List<FinalChallengePassedEvent> events() {
        return events;
    }
}
