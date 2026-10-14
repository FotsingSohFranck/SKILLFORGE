package dev.skillforge.catalogue.application;

import dev.skillforge.catalogue.domain.model.Roadmap;
import dev.skillforge.catalogue.domain.port.in.SearchRoadmapsUseCase;
import dev.skillforge.catalogue.domain.port.out.RoadmapRepository;
import reactor.core.publisher.Flux;

public class SearchRoadmapsService implements SearchRoadmapsUseCase {

    private final RoadmapRepository roadmaps;

    public SearchRoadmapsService(RoadmapRepository roadmaps) {
        this.roadmaps = roadmaps;
    }

    @Override
    public Flux<Roadmap> search(String query) {
        if (query == null || query.isBlank()) {
            return roadmaps.findAllOrdered();
        }
        return roadmaps.search(query.trim());
    }
}
