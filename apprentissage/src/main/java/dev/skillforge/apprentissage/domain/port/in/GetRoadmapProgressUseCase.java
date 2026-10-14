package dev.skillforge.apprentissage.domain.port.in;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.model.RoadmapProgress;
import reactor.core.publisher.Mono;

/** Obtenir la progression d'un apprenant sur un parcours (404 si non inscrit). */
public interface GetRoadmapProgressUseCase {
    Mono<RoadmapProgress> get(LearnerId learner, RoadmapId roadmap);
}
