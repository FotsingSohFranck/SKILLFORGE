package dev.skillforge.apprentissage.domain.port.out;

import dev.skillforge.apprentissage.domain.model.EnrollmentId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.NodeProgress;
import dev.skillforge.apprentissage.domain.model.ProgressStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/** Port sortant : progression par nœud. */
public interface NodeProgressStore {

    /** Upsert de la progression d'un nœud (dernière leçon = reprise exacte). */
    Mono<Void> upsert(EnrollmentId enrollment, NodeId node, ProgressStatus status,
                      double percent, LessonId lastLesson, Instant at);

    Flux<NodeProgress> findByEnrollment(EnrollmentId enrollment);

    /** Nombre de nœuds complétés (pour le % du parcours). */
    Mono<Long> countCompleted(EnrollmentId enrollment);

    /** Statut actuel du nœud ({@link Mono#empty()} si aucune progression enregistrée). */
    Mono<ProgressStatus> status(EnrollmentId enrollment, NodeId node);

    /** Vrai si l'épreuve pratique du nœud a été réussie par cette inscription. */
    Mono<Boolean> isPracticalPassed(EnrollmentId enrollment, NodeId node);

    /** Marque l'épreuve pratique réussie (crée la ligne si besoin), sans toucher au reste. */
    Mono<Void> markPracticalPassed(EnrollmentId enrollment, NodeId node, Instant at);

    /** Met à jour uniquement statut et pourcentage (sans toucher à la reprise ni à la pratique). */
    Mono<Void> updateStatus(EnrollmentId enrollment, NodeId node, ProgressStatus status,
                            double percent, Instant at);
}
