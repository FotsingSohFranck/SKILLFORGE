package dev.skillforge.catalogue.domain.port.out;

import dev.skillforge.catalogue.domain.model.Edge;
import dev.skillforge.catalogue.domain.model.RoadmapId;
import reactor.core.publisher.Flux;

/** Port sortant : accès aux arêtes d'un parcours. */
public interface EdgeRepository {

    Flux<Edge> findByRoadmapId(RoadmapId roadmapId);
}
