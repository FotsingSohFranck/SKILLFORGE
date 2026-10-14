package dev.skillforge.iam.application;

import dev.skillforge.iam.domain.port.in.LogoutUseCase;
import dev.skillforge.iam.domain.port.out.RefreshTokenStore;
import reactor.core.publisher.Mono;

public class LogoutService implements LogoutUseCase {

    private final RefreshTokenStore refreshTokenStore;

    public LogoutService(RefreshTokenStore refreshTokenStore) {
        this.refreshTokenStore = refreshTokenStore;
    }

    @Override
    public Mono<Void> logout(String refreshToken) {
        return refreshTokenStore.revoke(refreshToken);
    }
}
