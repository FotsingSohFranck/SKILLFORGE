package dev.skillforge.evaluation.adapter.in.web.dto;

import java.util.List;

/** Résultat de validation d'une soumission (solution révélée après soumission). */
public record SubmissionResultDto(
        boolean passed,
        List<String> matched,
        List<String> missing,
        String solution) {
}
