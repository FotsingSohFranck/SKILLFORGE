package dev.skillforge.contenu.domain.model;

import java.util.UUID;

/** Identifiant typé d'un nœud (réf souple vers catalogue.nodes). */
public record NodeId(UUID value) {
}
