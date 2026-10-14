package dev.skillforge.catalogue.domain.port.out;

import dev.skillforge.catalogue.domain.model.Phase;
import dev.skillforge.catalogue.domain.model.RoadmapId;
import reactor.core.publisher.Flux;

/** Port sortant : accès aux phases d'un parcours. */
public interface PhaseRepository {

    Flux<Phase> findByRoadmapId(RoadmapId roadmapId);
}
