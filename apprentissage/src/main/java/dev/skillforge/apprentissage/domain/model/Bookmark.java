package dev.skillforge.apprentissage.domain.model;

import java.util.UUID;

/** Favori / marque-page sur un nœud ou une leçon. */
public record Bookmark(BookmarkTarget targetType, UUID targetId) {
}
