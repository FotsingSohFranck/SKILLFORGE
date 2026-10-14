package dev.skillforge.iam.adapter.in.web.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Corps de requête : nouveaux rôles d'un utilisateur (ex. ["LEARNER","INSTRUCTOR"]). */
public record ChangeRolesRequest(
        @NotEmpty List<String> roles) {
}
