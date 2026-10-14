package dev.skillforge.catalogue.domain.port.out;

import dev.skillforge.catalogue.domain.model.Certification;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Port sortant : lecture de la certification (et ses domaines) rattachée à un parcours. */
public interface CertificationRepository {

    Mono<Certification> findByRoadmapSlug(String slug);

    /** Lecture directe par identifiant de certification (résultats d'examen → libellés domaines). */
    Mono<Certification> findById(UUID id);
}
