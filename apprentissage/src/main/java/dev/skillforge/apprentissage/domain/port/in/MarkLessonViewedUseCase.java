package dev.skillforge.apprentissage.domain.port.in;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import reactor.core.publisher.Mono;

/** Marquer une leçon comme vue : met à jour la reprise (nœud/leçon) et la progression. */
public interface MarkLessonViewedUseCase {
    Mono<Void> markViewed(LearnerId learner, RoadmapId roadmap, NodeId node, LessonId lesson);
}
