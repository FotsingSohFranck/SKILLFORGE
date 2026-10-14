package dev.skillforge.common.event;

import java.util.UUID;

/** Événement d'intégration : un examen blanc a été réussi (publié par évaluation). */
public record ExamPassedEvent(UUID learnerId, UUID examId, UUID certificationId) {
}
