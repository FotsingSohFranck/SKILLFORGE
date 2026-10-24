package dev.skillforge.apprentissage.adapter.in.event;

import dev.skillforge.apprentissage.domain.port.in.MarkNodePracticalPassedUseCase;
import dev.skillforge.common.event.PracticalPassedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Consomme {@code PracticalPassedEvent} (émis par le contexte lab) : enregistre la réussite de
 * l'épreuve pratique sur le nœud et recalcule la progression (gating de la validation du nœud).
 */
@Component
public class PracticalProgressListener {

    private final MarkNodePracticalPassedUseCase markPassed;

    public PracticalProgressListener(MarkNodePracticalPassedUseCase markPassed) {
        this.markPassed = markPassed;
    }

    @EventListener
    public void onPracticalPassed(PracticalPassedEvent event) {
        markPassed.markPassed(event.learnerId(), event.nodeId()).subscribe();
    }
}
