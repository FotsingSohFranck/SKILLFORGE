package dev.skillforge.apprentissage.domain.port.in;

import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import reactor.core.publisher.Mono;

/** Inscrire un apprenant à un parcours (idempotent : renvoie l'inscription existante le cas échéant). */
public interface EnrollLearnerUseCase {
    Mono<Enrollment> enroll(LearnerId learner, RoadmapId roadmap);
}
