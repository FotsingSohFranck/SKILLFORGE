package dev.skillforge.iam.adapter.out.security;

import dev.skillforge.iam.config.SecurityJwtProperties;
import dev.skillforge.iam.domain.model.AccessToken;
import dev.skillforge.iam.domain.model.RoleCode;
import dev.skillforge.iam.domain.model.User;
import dev.skillforge.iam.domain.port.out.AccessTokenIssuer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Émetteur de jetons d'accès JWT (HS256) : subject = userId, claims = email + roles. */
@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuer {

    private final JwtEncoder encoder;
    private final Duration ttl;

    public JwtAccessTokenIssuer(JwtEncoder encoder, SecurityJwtProperties props) {
        this.encoder = encoder;
        this.ttl = Duration.ofMinutes(props.accessTtlMinutes());
    }

    @Override
    public AccessToken issue(User user) {
        Instant now = Instant.now();
        List<String> roles = user.roles().stream().map(RoleCode::name).toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.id().value().toString())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim("email", user.email().value())
                .claim("roles", roles)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessToken(token, ttl.toSeconds());
    }
}
