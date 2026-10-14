package dev.skillforge.apprentissage.domain.port.in;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import reactor.core.publisher.Mono;

/** Marquer une leçon comme complétée : recalcule les % de nœud et de parcours. */
public interface CompleteLessonUseCase {
    Mono<Void> complete(LearnerId learner, RoadmapId roadmap, NodeId node, LessonId lesson);
}
