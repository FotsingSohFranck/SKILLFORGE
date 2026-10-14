package dev.skillforge.catalogue.application;

import dev.skillforge.catalogue.domain.model.Roadmap;
import dev.skillforge.catalogue.domain.port.in.ListRoadmapsUseCase;
import dev.skillforge.catalogue.domain.port.out.RoadmapRepository;
import reactor.core.publisher.Flux;

public class ListRoadmapsService implements ListRoadmapsUseCase {

    private final RoadmapRepository roadmaps;

    public ListRoadmapsService(RoadmapRepository roadmaps) {
        this.roadmaps = roadmaps;
    }

    @Override
    public Flux<Roadmap> list() {
        return roadmaps.findAllOrdered();
    }
}
