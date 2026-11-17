package dev.skillforge.challenge.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Soumission d'une décision : identifiant du choix retenu sur le nœud courant. */
public record SubmitDecisionRequest(@NotBlank String choiceId) {
}
