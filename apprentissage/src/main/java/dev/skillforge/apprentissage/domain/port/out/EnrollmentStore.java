package dev.skillforge.apprentissage.domain.port.out;

import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.EnrollmentId;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/** Port sortant : persistance des inscriptions et de l'état de reprise. */
public interface EnrollmentStore {

    Mono<Enrollment> findByLearnerAndRoadmap(LearnerId learner, RoadmapId roadmap);

    /** Toutes les inscriptions de l'apprenant (parcours en cours, du plus récent au plus ancien). */
    Flux<Enrollment> findByLearner(LearnerId learner);

    /** Crée l'inscription si absente (idempotent) et la renvoie. */
    Mono<Enrollment> create(LearnerId learner, RoadmapId roadmap);

    /** Met à jour l'état de reprise + le % du parcours. */
    Mono<Void> updateProgress(EnrollmentId id, NodeId lastNode, LessonId lastLesson,
                              double percent, Instant at);

    /** Dernière inscription active (reprise globale) ; vide si aucune activité. */
    Mono<Enrollment> findLastActive(LearnerId learner);
}
