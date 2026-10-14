package dev.skillforge.apprentissage.domain.model;

import java.time.Instant;

/**
 * Inscription d'un apprenant à un parcours + état de <b>reprise</b> (dernier nœud / dernière
 * leçon actifs). {@code lastActive*} et {@code lastActivityAt} sont nuls tant qu'aucune activité.
 */
public record Enrollment(
        EnrollmentId id,
        LearnerId learnerId,
        RoadmapId roadmapId,
        double progressPercent,
        NodeId lastActiveNodeId,
        LessonId lastActiveLessonId,
        Instant lastActivityAt) {
}
