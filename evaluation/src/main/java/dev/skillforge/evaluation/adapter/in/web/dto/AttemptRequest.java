package dev.skillforge.evaluation.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/** Corps de requête : réponses soumises. */
public record AttemptRequest(
        @NotNull List<AnswerInput> answers) {

    public record AnswerInput(UUID questionId, UUID optionId) {
    }
}
