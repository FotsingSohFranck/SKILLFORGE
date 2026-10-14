package dev.skillforge.iam.adapter.out.persistence;

import dev.skillforge.common.error.UnauthorizedException;
import dev.skillforge.iam.config.SecurityJwtProperties;
import dev.skillforge.iam.domain.model.UserId;
import dev.skillforge.iam.domain.port.out.RefreshTokenStore;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Store R2DBC des refresh tokens (table {@code refresh_tokens}).
 * Le token brut (haute entropie) n'est jamais stocké : seule son empreinte SHA-256 l'est,
 * ce qui permet la recherche déterministe. Rotation à chaque usage.
 */
@Component
public class RefreshTokenR2dbcStore implements RefreshTokenStore {

    private static final String INSERT_SQL =
            "INSERT INTO refresh_tokens(user_id, token_hash, expires_at, revoked) "
                    + "VALUES (:uid, :hash, :exp, false)";
    private static final String ROTATE_SQL =
            "UPDATE refresh_tokens SET revoked = true "
                    + "WHERE token_hash = :hash AND revoked = false AND expires_at > now() "
                    + "RETURNING user_id";
    private static final String REVOKE_SQL =
            "UPDATE refresh_tokens SET revoked = true WHERE token_hash = :hash";

    private final DatabaseClient db;
    private final Duration ttl;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenR2dbcStore(R2dbcEntityTemplate template, SecurityJwtProperties props) {
        this.db = template.getDatabaseClient();
        this.ttl = Duration.ofDays(props.refreshTtlDays());
    }

    @Override
    public Mono<String> issue(UserId userId) {
        String raw = generateRawToken();
        OffsetDateTime expiresAt = OffsetDateTime.ofInstant(Instant.now().plus(ttl), ZoneOffset.UTC);
        return db.sql(INSERT_SQL)
                .bind("uid", userId.value())
                .bind("hash", sha256(raw))
                .bind("exp", expiresAt)
                .fetch().rowsUpdated()
                .thenReturn(raw);
    }

    @Override
    public Mono<UserId> rotate(String rawToken) {
        return db.sql(ROTATE_SQL)
                .bind("hash", sha256(rawToken))
                .map((row, meta) -> new UserId(row.get("user_id", UUID.class)))
                .one()
                .switchIfEmpty(Mono.error(new UnauthorizedException(
                        "INVALID_REFRESH_TOKEN", "Refresh token invalide ou expiré.")));
    }

    @Override
    public Mono<Void> revoke(String rawToken) {
        return db.sql(REVOKE_SQL)
                .bind("hash", sha256(rawToken))
                .fetch().rowsUpdated()
                .then();
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
