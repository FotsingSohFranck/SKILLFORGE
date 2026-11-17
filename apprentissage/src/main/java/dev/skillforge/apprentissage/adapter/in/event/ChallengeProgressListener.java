package dev.skillforge.apprentissage.adapter.in.event;

import dev.skillforge.apprentissage.domain.port.in.MarkNodeChallengePassedUseCase;
import dev.skillforge.common.event.FinalChallengePassedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Consomme {@code FinalChallengePassedEvent} (émis par le contexte challenge) : enregistre la
 * réussite du Final Challenge sur le nœud et recalcule la progression (gating de validation du nœud).
 */
@Component
public class ChallengeProgressListener {

    private final MarkNodeChallengePassedUseCase markPassed;

    public ChallengeProgressListener(MarkNodeChallengePassedUseCase markPassed) {
        this.markPassed = markPassed;
    }

    @EventListener
    public void onChallengePassed(FinalChallengePassedEvent event) {
        markPassed.markPassed(event.learnerId(), event.nodeId()).subscribe();
    }
}
