package dev.skillforge.catalogue.domain.port.out;

import dev.skillforge.catalogue.domain.model.Roadmap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Port sortant : accès aux parcours. */
public interface RoadmapRepository {

    Flux<Roadmap> findAllOrdered();

    Mono<Roadmap> findBySlug(String slug);

    Flux<Roadmap> search(String query);
}
