package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.ProgressStatus;

/** Résultat d'un recalcul de progression : statut du nœud et % du parcours après l'opération. */
public record ProgressOutcome(ProgressStatus nodeStatus, double roadmapPercent) {
}
