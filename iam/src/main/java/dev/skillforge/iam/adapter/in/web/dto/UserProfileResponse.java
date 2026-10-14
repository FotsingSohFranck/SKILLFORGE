package dev.skillforge.iam.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

/** Représentation web du profil utilisateur. */
public record UserProfileResponse(
        UUID id,
        String email,
        String displayName,
        boolean enabled,
        List<String> roles) {
}
