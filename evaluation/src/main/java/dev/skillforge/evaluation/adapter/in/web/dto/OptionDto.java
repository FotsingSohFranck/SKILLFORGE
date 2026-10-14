package dev.skillforge.evaluation.adapter.in.web.dto;

import java.util.UUID;

/** Option de réponse exposée pour le passage (sans révéler si elle est correcte). */
public record OptionDto(UUID id, String label, int position) {
}
