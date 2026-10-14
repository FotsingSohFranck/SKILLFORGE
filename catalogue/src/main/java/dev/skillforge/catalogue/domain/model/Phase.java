package dev.skillforge.catalogue.domain.model;

/** Regroupement ordonné de nœuds au sein d'un parcours. */
public record Phase(
        PhaseId id,
        String title,
        int position) {
}
