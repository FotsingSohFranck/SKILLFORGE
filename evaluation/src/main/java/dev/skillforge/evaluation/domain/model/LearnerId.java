package dev.skillforge.evaluation.domain.model;

import java.util.UUID;

/** Identifiant typé d'un apprenant (réf souple vers iam.users). */
public record LearnerId(UUID value) {
}
