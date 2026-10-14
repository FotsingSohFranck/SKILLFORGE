package dev.skillforge.catalogue.domain.model;

import java.util.UUID;

/** Domaine d'examen d'une certification (pondéré), pour la couverture et la révision ciblée. */
public record ExamDomain(
        UUID id,
        String name,
        double weight,
        int position) {
}
