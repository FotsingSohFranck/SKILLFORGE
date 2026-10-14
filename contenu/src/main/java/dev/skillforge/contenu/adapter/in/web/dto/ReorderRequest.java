package dev.skillforge.contenu.adapter.in.web.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/** Corps de requête : nouvel ordre (liste d'identifiants). */
public record ReorderRequest(
        @NotEmpty List<UUID> ids) {
}
