package dev.skillforge.apprentissage.it;

import dev.skillforge.common.event.LessonCompletedEvent;
import dev.skillforge.common.event.NodeCompletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Capture les événements d'intégration publiés sur le bus Spring (pour les tests). */
@Component
public class CapturingLearningEventListener {

    private final List<Object> events = Collections.synchronizedList(new ArrayList<>());

    @EventListener
    void onLessonCompleted(LessonCompletedEvent event) {
        events.add(event);
    }

    @EventListener
    void onNodeCompleted(NodeCompletedEvent event) {
        events.add(event);
    }

    List<Object> events() {
        return events;
    }

    void clear() {
        events.clear();
    }
}
