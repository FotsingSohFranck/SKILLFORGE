package dev.skillforge.evaluation.domain.model;

import java.util.UUID;

/** Identifiant typé d'un item d'examen (question QCM ou tâche). */
public record ExamItemId(UUID value) {
}
