package dev.skillforge.common.event;

import java.util.UUID;

/** Événement d'intégration : la série d'apprentissage a évolué (publié par apprentissage). */
public record StreakUpdatedEvent(UUID learnerId, int currentLength) {
}
