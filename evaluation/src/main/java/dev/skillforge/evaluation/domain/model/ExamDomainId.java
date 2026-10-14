package dev.skillforge.evaluation.domain.model;

import java.util.UUID;

/** Identifiant typé d'un domaine d'examen (réf souple vers catalogue.exam_domains). */
public record ExamDomainId(UUID value) {
}
