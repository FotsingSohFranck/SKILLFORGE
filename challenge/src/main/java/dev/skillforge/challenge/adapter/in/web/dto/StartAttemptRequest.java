package dev.skillforge.challenge.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Démarrage d'une tentative : nœud (de fin) dont on joue le Final Challenge. */
public record StartAttemptRequest(@NotNull UUID nodeId) {
}
