package dev.skillforge.catalogue.domain.port.in;

import dev.skillforge.catalogue.domain.model.RoadmapGraph;
import reactor.core.publisher.Mono;

/** Obtenir un parcours et son graphe (phases, nœuds, arêtes) par slug. */
public interface GetRoadmapGraphUseCase {
    Mono<RoadmapGraph> getBySlug(String slug);
}
