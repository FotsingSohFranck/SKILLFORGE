package dev.skillforge.iam.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Sécurité réactive de la plateforme (remplace la config permissive temporaire de SF-11a).
 * Authentification par JWT HS256 (Resource Server). Les routes d'auth et la documentation
 * sont publiques ; tout le reste requiert un jeton valide.
 */
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityJwtProperties.class)
public class IamSecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/register", "/api/v1/auth/login",
            "/api/v1/auth/refresh", "/api/v1/auth/logout",
            // Flux "mot de passe oublié" (non authentifié). Le changement de mot de passe
            // connecté (/api/v1/auth/password en PATCH) reste protégé par anyExchange().
            "/api/v1/auth/password/forgot", "/api/v1/auth/password/reset",
            "/actuator/health", "/actuator/info",
            "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/webjars/**"
    };

    private SecretKeySpec secretKey(SecurityJwtProperties props) {
        return new SecretKeySpec(props.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecurityJwtProperties props) {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretKey(props)));
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder(SecurityJwtProperties props) {
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey(props))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PUBLIC_PATHS).permitAll()
                        .pathMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // Ingestion / authoring de contenu : réservé aux créateurs.
                        .pathMatchers(HttpMethod.POST, "/api/v1/roadmaps/import")
                        .hasAnyRole("INSTRUCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/nodes/*/content/import")
                        .hasAnyRole("INSTRUCTOR", "ADMIN")
                        // Authoring de contenu (leçons & sections) : écriture réservée aux créateurs.
                        .pathMatchers(HttpMethod.POST, "/api/v1/nodes/*/lessons").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/nodes/*/lessons/order").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/lessons/*").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/lessons/*").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/lessons/*/sections").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/lessons/*/sections/order").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/sections/*").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/sections/*").hasAnyRole("INSTRUCTOR", "ADMIN")
                        // Ingestion des quiz / défis (évaluation) : réservée aux créateurs.
                        .pathMatchers(HttpMethod.POST, "/api/v1/quizzes/import").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/challenges/import").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/exams/import").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    /** Convertit le claim {@code roles} en autorités {@code ROLE_*}. */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
