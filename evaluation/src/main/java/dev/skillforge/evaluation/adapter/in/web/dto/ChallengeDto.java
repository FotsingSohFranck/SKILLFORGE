package dev.skillforge.evaluation.adapter.in.web.dto;

import java.util.UUID;

/** Défi exposé pour la résolution (sans solution ni points clés). */
public record ChallengeDto(
        UUID id,
        String title,
        String statement,
        String skeleton,
        String language) {
}
