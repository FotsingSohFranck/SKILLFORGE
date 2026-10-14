package dev.skillforge.catalogue.adapter.in.web.dto;

import java.util.UUID;

/** Représentation web d'une arête (prérequis indicatif). */
public record EdgeDto(
        UUID id,
        UUID sourceNodeId,
        UUID targetNodeId,
        String kind) {
}
