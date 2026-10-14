package dev.skillforge.apprentissage.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

/** Représentation web des leçons complétées d'un nœud (identifiants). */
public record NodeLessonProgressDto(List<UUID> completedLessonIds) {
}
