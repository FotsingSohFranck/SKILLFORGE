package dev.skillforge.apprentissage.domain.model;

import java.util.UUID;

/** Identifiant typé d'un apprenant (réf souple vers iam.users). */
public record LearnerId(UUID value) {
}
