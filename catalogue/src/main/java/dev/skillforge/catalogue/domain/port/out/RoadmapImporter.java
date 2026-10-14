package dev.skillforge.catalogue.domain.port.out;

import dev.skillforge.catalogue.domain.model.RoadmapId;
import dev.skillforge.catalogue.domain.port.in.RoadmapImport;
import reactor.core.publisher.Mono;

/**
 * Port sortant : persistance idempotente d'une roadmap importée (upsert par clés naturelles,
 * préservation des identifiants existants). Cf. docs/PIVOT-ROADMAP.md.
 */
public interface RoadmapImporter {
    Mono<RoadmapId> importRoadmap(RoadmapImport command);
}
