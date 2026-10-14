package dev.skillforge.iam.application;

import dev.skillforge.common.error.UnauthorizedException;
import dev.skillforge.iam.domain.model.AccessToken;
import dev.skillforge.iam.domain.model.AuthTokens;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.port.in.RefreshTokenUseCase;
import dev.skillforge.iam.domain.port.out.AccessTokenIssuer;
import dev.skillforge.iam.domain.port.out.RefreshTokenStore;
import dev.skillforge.iam.domain.port.out.UserRepository;
import reactor.core.publisher.Mono;

public class RefreshTokenService implements RefreshTokenUseCase {

    private final UserRepository users;
    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenStore refreshTokenStore;

    public RefreshTokenService(UserRepository users,
                               AccessTokenIssuer accessTokenIssuer,
                               RefreshTokenStore refreshTokenStore) {
        this.users = users;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Override
    public Mono<AuthTokens> refresh(String refreshToken) {
        return refreshTokenStore.rotate(refreshToken)
                .flatMap(userId -> users.findById(userId)
                        .filter(User::enabled)
                        .switchIfEmpty(Mono.error(new UnauthorizedException(
                                "INVALID_REFRESH_TOKEN", "Session invalide."))))
                .flatMap(user -> {
                    AccessToken access = accessTokenIssuer.issue(user);
                    return refreshTokenStore.issue(user.id())
                            .map(newRefresh -> AuthTokens.bearer(
                                    access.value(), newRefresh, access.expiresInSeconds()));
                });
    }
}
