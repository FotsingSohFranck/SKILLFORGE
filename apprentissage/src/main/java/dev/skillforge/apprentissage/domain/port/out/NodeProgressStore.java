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
}
