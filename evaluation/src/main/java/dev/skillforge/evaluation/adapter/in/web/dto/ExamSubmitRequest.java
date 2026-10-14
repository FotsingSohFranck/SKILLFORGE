package dev.skillforge.evaluation.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/** Corps de requête : réponses d'un examen (option pour MCQ, completed pour tâche). */
public record ExamSubmitRequest(
        @NotNull List<ItemAnswerInput> answers) {

    public record ItemAnswerInput(UUID itemId, UUID selectedOptionId, Boolean completed) {
    }
}
