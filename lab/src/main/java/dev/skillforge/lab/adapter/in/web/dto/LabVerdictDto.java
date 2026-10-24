package dev.skillforge.lab.adapter.in.web.dto;

import dev.skillforge.lab.domain.port.in.PracticalVerdict;

import java.util.UUID;

/** Réponse : verdict d'une soumission pratique. */
public record LabVerdictDto(UUID attemptId, boolean passed, String output) {

    public static LabVerdictDto from(PracticalVerdict v) {
        return new LabVerdictDto(v.attemptId(), v.passed(), v.output());
    }
}
