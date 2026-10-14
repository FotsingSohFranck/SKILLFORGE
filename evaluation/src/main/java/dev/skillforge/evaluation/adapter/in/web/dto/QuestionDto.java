package dev.skillforge.evaluation.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

/** Question exposée pour le passage (sans bonne réponse ni justification). */
public record QuestionDto(
        UUID id,
        String type,
        String prompt,
        int position,
        List<OptionDto> options) {
}
