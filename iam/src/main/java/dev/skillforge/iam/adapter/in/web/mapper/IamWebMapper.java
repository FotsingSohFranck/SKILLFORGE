package dev.skillforge.iam.adapter.in.web.mapper;

import dev.skillforge.iam.adapter.in.web.dto.TokensResponse;
import dev.skillforge.iam.adapter.in.web.dto.UserProfileResponse;
import dev.skillforge.iam.domain.model.AuthTokens;
import dev.skillforge.iam.domain.model.RoleCode;
import dev.skillforge.iam.domain.model.User;

/** Traduction domaine → DTO web (hors du domaine). */
public final class IamWebMapper {

    private IamWebMapper() {
    }

    public static UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
                user.id().value(),
                user.email().value(),
                user.displayName(),
                user.enabled(),
                user.roles().stream().map(RoleCode::name).sorted().toList());
    }

    public static TokensResponse toTokens(AuthTokens tokens) {
        return new TokensResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.tokenType(),
                tokens.expiresInSeconds());
    }
}
