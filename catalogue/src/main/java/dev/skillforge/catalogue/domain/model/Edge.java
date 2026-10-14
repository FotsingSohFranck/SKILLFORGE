package dev.skillforge.catalogue.domain.model;

/** Arête orientée entre deux nœuds (prérequis indicatif, non bloquant). */
public record Edge(
        EdgeId id,
        NodeId sourceNodeId,
        NodeId targetNodeId,
        EdgeKind kind) {
}
