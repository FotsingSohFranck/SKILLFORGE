package dev.skillforge.catalogue.application;

import dev.skillforge.catalogue.domain.model.RoadmapGraph;
import dev.skillforge.catalogue.domain.port.in.GetRoadmapGraphUseCase;
import dev.skillforge.catalogue.domain.port.out.EdgeRepository;
import dev.skillforge.catalogue.domain.port.out.NodeRepository;
import dev.skillforge.catalogue.domain.port.out.PhaseRepository;
import dev.skillforge.catalogue.domain.port.out.RoadmapRepository;
import dev.skillforge.common.error.NotFoundException;
import reactor.core.publisher.Mono;

public class GetRoadmapGraphService implements GetRoadmapGraphUseCase {

    private final RoadmapRepository roadmaps;
    private final PhaseRepository phases;
    private final NodeRepository nodes;
    private final EdgeRepository edges;

    public GetRoadmapGraphService(RoadmapRepository roadmaps, PhaseRepository phases,
                                  NodeRepository nodes, EdgeRepository edges) {
        this.roadmaps = roadmaps;
        this.phases = phases;
        this.nodes = nodes;
        this.edges = edges;
    }

    @Override
    public Mono<RoadmapGraph> getBySlug(String slug) {
        return roadmaps.findBySlug(slug)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "ROADMAP_NOT_FOUND", "Parcours introuvable : " + slug)))
                .flatMap(roadmap -> Mono.zip(
                        phases.findByRoadmapId(roadmap.id()).collectList(),
                        nodes.findByRoadmapId(roadmap.id()).collectList(),
                        edges.findByRoadmapId(roadmap.id()).collectList()
                ).map(tuple -> new RoadmapGraph(
                        roadmap, tuple.getT1(), tuple.getT2(), tuple.getT3())));
    }
}
