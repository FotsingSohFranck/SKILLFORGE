package dev.skillforge.apprentissage.adapter.in.web.dto;

import java.util.UUID;

/** Représentation web d'un favori. */
public record BookmarkDto(String targetType, UUID targetId) {
}
