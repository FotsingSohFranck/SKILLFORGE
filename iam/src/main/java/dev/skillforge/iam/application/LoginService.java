package dev.skillforge.iam.application;

import dev.skillforge.iam.domain.model.AccessToken;
import dev.skillforge.iam.domain.model.AuthTokens;
import dev.skillforge.iam.domain.port.in.AuthenticateUserUseCase;
import dev.skillforge.iam.domain.port.in.LoginUseCase;
import dev.skillforge.iam.domain.port.out.AccessTokenIssuer;
import dev.skillforge.iam.domain.port.out.RefreshTokenStore;
import reactor.core.publisher.Mono;

public class LoginService implements LoginUseCase {

    private final AuthenticateUserUseCase authenticate;
    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenStore refreshTokenStore;

    public LoginService(AuthenticateUserUseCase authenticate,
                        AccessTokenIssuer accessTokenIssuer,
                        RefreshTokenStore refreshTokenStore) {
        this.authenticate = authenticate;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Override
    public Mono<AuthTokens> login(String email, String rawPassword) {
        return authenticate.authenticate(email, rawPassword)
                .flatMap(user -> {
                    AccessToken access = accessTokenIssuer.issue(user);
                    return refreshTokenStore.issue(user.id())
                            .map(refresh -> AuthTokens.bearer(
                                    access.value(), refresh, access.expiresInSeconds()));
                });
    }
}
