package dev.skillforge.gamification.application;

import dev.skillforge.gamification.domain.model.GamifiedProfile;
import dev.skillforge.gamification.domain.model.LearnerId;
import dev.skillforge.gamification.domain.model.Level;
import dev.skillforge.gamification.domain.port.in.GetGamifiedProfileUseCase;
import dev.skillforge.gamification.domain.port.out.LevelRepository;
import dev.skillforge.gamification.domain.port.out.XpLedger;
import reactor.core.publisher.Mono;

import java.util.List;

/** Calcule le profil gamifié : palier courant (plus haut seuil ≤ XP) et prochain palier. */
public class GetGamifiedProfileService implements GetGamifiedProfileUseCase {

    private final XpLedger ledger;
    private final LevelRepository levels;

    public GetGamifiedProfileService(XpLedger ledger, LevelRepository levels) {
        this.ledger = ledger;
        this.levels = levels;
    }

    @Override
    public Mono<GamifiedProfile> get(LearnerId learner) {
        return Mono.zip(ledger.totalXp(learner), levels.findAllOrdered().collectList())
                .map(tuple -> profile(tuple.getT1(), tuple.getT2()));
    }

    private GamifiedProfile profile(long totalXp, List<Level> ordered) {
        Level current = null;
        Level next = null;
        for (Level level : ordered) {
            if (level.minXp() <= totalXp) {
                current = level;
            } else if (next == null) {
                next = level;
            }
        }
        long xpToNext = next == null ? 0 : Math.max(0, next.minXp() - totalXp);
        return new GamifiedProfile(totalXp, current, next, xpToNext);
    }
}
