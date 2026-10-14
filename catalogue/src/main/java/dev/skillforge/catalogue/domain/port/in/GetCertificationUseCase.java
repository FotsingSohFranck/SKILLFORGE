package dev.skillforge.catalogue.domain.port.in;

import dev.skillforge.catalogue.domain.model.Certification;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Cas d'usage : obtenir la certification (et ses domaines) d'un parcours par slug ou par id. */
public interface GetCertificationUseCase {

    Mono<Certification> getByRoadmapSlug(String slug);

    Mono<Certification> getById(UUID id);
}
