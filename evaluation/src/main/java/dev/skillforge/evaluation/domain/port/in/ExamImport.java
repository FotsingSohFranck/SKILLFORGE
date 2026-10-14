package dev.skillforge.evaluation.domain.port.in;

import java.util.List;
import java.util.UUID;

/** Commande d'import d'un examen blanc (pivot). Upsert par {@code id} (idempotent). */
public record ExamImport(
        UUID id,
        UUID certificationId,
        String title,
        String format,
        Integer durationMinutes,
        Double passingScore,
        List<ItemImport> items) {

    /**
     * Item d'examen. {@code MCQ} référence une question de quiz existante ({@code questionId}) ;
     * {@code TASK} porte un énoncé ({@code prompt}) et une solution attendue ({@code taskSolution}).
     */
    public record ItemImport(
            UUID examDomainId,
            String kind,
            UUID questionId,
            String prompt,
            String taskSolution,
            Double points,
            Integer position) {
    }
}
