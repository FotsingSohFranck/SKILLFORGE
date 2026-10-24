package dev.skillforge.lab.adapter.in.web.dto;

import dev.skillforge.lab.domain.port.in.PracticalView;

/** Vue de l'épreuve pratique d'un nœud (réponse du GET). */
public record LabPracticalDto(
        boolean exists,
        boolean required,
        String title,
        String instructions,
        String latestStatus,
        boolean latestPassed
) {
    public static LabPracticalDto from(PracticalView v) {
        return new LabPracticalDto(v.exists(), v.required(), v.title(), v.instructions(),
                v.latestStatus(), v.latestPassed());
    }
}
