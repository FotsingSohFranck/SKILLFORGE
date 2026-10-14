package dev.skillforge.gamification.domain.port.in;

import dev.skillforge.gamification.domain.model.GamifiedProfile;
import dev.skillforge.gamification.domain.model.LearnerId;
import reactor.core.publisher.Mono;

/** Obtenir le profil gamifié d'un apprenant (niveau, XP, prochain palier). */
public interface GetGamifiedProfileUseCase {
    Mono<GamifiedProfile> get(LearnerId learner);
}
