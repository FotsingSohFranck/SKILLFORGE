package dev.skillforge.apprentissage.domain.port.out;

import dev.skillforge.apprentissage.domain.model.EnrollmentId;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/** Port sortant : progression par leçon. */
public interface LessonProgressStore {

    /** Marque une leçon comme vue (sans dégrader un statut COMPLETED existant). */
    Mono<Void> markViewed(EnrollmentId enrollment, NodeId node, LessonId lesson, Instant at);

    /** Marque une leçon comme complétée. */
    Mono<Void> markCompleted(EnrollmentId enrollment, NodeId node, LessonId lesson, Instant at);

    /** Nombre de leçons complétées dans un nœud (pour le % du nœud). */
    Mono<Long> countCompletedInNode(EnrollmentId enrollment, NodeId node);

    /** Identifiants des leçons complétées par l'apprenant dans un nœud (pour les coches). */
    Flux<LessonId> findCompletedLessonIds(LearnerId learner, NodeId node);
}
