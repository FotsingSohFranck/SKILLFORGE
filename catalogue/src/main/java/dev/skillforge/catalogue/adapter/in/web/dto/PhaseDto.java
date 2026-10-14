package dev.skillforge.catalogue.adapter.in.web.dto;

import java.util.UUID;

/** Représentation web d'une phase. */
public record PhaseDto(
        UUID id,
        String title,
        int position) {
}
