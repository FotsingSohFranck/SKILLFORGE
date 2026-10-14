package dev.skillforge.iam.adapter.out.persistence;

import dev.skillforge.iam.config.PasswordResetProperties;
import dev.skillforge.iam.domain.model.UserId;
import dev.skillforge.iam.domain.port.out.PasswordResetTokenRepository;
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
 * Store R2DBC des jetons de réinitialisation (table {@code password_reset_tokens}).
 * Seule l'empreinte SHA-256 du jeton est stockée. Jeton à usage unique (used_at) et expirant.
 */
@Component
public class PasswordResetTokenR2dbcStore implements PasswordResetTokenRepository {

    private static final String INSERT_SQL =
            "INSERT INTO password_reset_tokens(user_id, token_hash, expires_at) "
                    + "VALUES (:uid, :hash, :exp)";
    private static final String CONSUME_SQL =
            "UPDATE password_reset_tokens SET used_at = now() "
                    + "WHERE token_hash = :hash AND used_at IS NULL AND expires_at > now() "
                    + "RETURNING user_id";

    private final DatabaseClient db;
    private final Duration ttl;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetTokenR2dbcStore(R2dbcEntityTemplate template, PasswordResetProperties props) {
        this.db = template.getDatabaseClient();
        this.ttl = Duration.ofMinutes(props.ttlMinutes());
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
    public Mono<UserId> consume(String rawToken) {
        return db.sql(CONSUME_SQL)
                .bind("hash", sha256(rawToken))
                .map((row, meta) -> new UserId(row.get("user_id", UUID.class)))
                .one();
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
