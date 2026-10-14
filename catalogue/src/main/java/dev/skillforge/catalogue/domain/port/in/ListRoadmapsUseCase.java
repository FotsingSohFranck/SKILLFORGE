package dev.skillforge.catalogue.domain.port.in;

import dev.skillforge.catalogue.domain.model.Roadmap;
import reactor.core.publisher.Flux;

/** Lister tous les parcours du catalogue, ordonnés. */
public interface ListRoadmapsUseCase {
    Flux<Roadmap> list();
}
