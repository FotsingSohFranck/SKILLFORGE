package dev.skillforge.catalogue.application;

import dev.skillforge.catalogue.domain.model.Certification;
import dev.skillforge.catalogue.domain.port.in.GetCertificationUseCase;
import dev.skillforge.catalogue.domain.port.out.CertificationRepository;
import dev.skillforge.common.error.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Obtient la certification d'un parcours (404 si le parcours n'est pas une certification). */
public class GetCertificationService implements GetCertificationUseCase {

    private final CertificationRepository certifications;

    public GetCertificationService(CertificationRepository certifications) {
        this.certifications = certifications;
    }

    @Override
    public Mono<Certification> getByRoadmapSlug(String slug) {
        return certifications.findByRoadmapSlug(slug)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "CERTIFICATION_NOT_FOUND", "Aucune certification pour le parcours : " + slug)));
    }

    @Override
    public Mono<Certification> getById(UUID id) {
        return certifications.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "CERTIFICATION_NOT_FOUND", "Certification introuvable : " + id)));
    }
}
