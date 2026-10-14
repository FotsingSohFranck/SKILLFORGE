package dev.skillforge.evaluation.domain.port.in;

import java.util.List;
import java.util.UUID;

/** Commande d'import d'un défi de code (pivot). Upsert par {@code id} (idempotent). */
public record ChallengeImport(
        UUID id,
        UUID nodeId,
        UUID lessonId,
        String title,
        String statement,
        String skeleton,
        String solution,
        List<String> keyPoints,
        String language) {
}
