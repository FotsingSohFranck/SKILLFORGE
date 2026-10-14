package dev.skillforge.catalogue.domain.port.in;

import dev.skillforge.catalogue.domain.model.Roadmap;
import reactor.core.publisher.Flux;

/** Rechercher des parcours par texte (titre ou slug). */
public interface SearchRoadmapsUseCase {
    Flux<Roadmap> search(String query);
}
