package dev.skillforge.iam.it;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.iam.adapter.in.web.dto.TokensResponse;
import dev.skillforge.iam.adapter.in.web.dto.UserProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test d'intégration IAM : flux réel register → login → appel protégé /me → refresh (rotation),
 * contre PostgreSQL 18 (Testcontainers) avec la vraie chaîne de sécurité JWT.
 */
@SpringBootTest(classes = IamITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("skillforge")
                    .withInitScript("db/iam-it.sql");

    @DynamicPropertySource
    static void r2dbcProps(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
                + POSTGRES.getHost() + ":" + POSTGRES.getFirstMappedPort()
                + "/" + POSTGRES.getDatabaseName());
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
    }

    private static final ParameterizedTypeReference<ApiResponse<TokensResponse>> TOKENS =
            new ParameterizedTypeReference<>() {
            };

    @Value("${local.server.port}")
    int port;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void flux_complet_register_login_me_refresh() {
        String email = "marc@demo.dev";
        String password = "topsecret123";

        // 1) Inscription -> 201, rôle LEARNER
        client.post().uri("/api/v1/auth/register")
                .bodyValue(Map.of("email", email, "password", password, "displayName", "Marc"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.email").isEqualTo(email)
                .jsonPath("$.data.roles[0]").isEqualTo("LEARNER");

        // 2) /me sans jeton -> 401
        client.get().uri("/api/v1/auth/me").exchange().expectStatus().isUnauthorized();

        // 3) Connexion -> 200, paire de jetons
        TokensResponse tokens = login(email, password);
        assertNotNull(tokens.accessToken());
        assertNotNull(tokens.refreshToken());

        // 4) /me avec le jeton d'accès -> 200
        client.get().uri("/api/v1/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.email").isEqualTo(email)
                .jsonPath("$.data.roles[0]").isEqualTo("LEARNER");

        // 5) Refresh -> 200, nouveau refresh token (rotation)
        ApiResponse<TokensResponse> refreshed = client.post().uri("/api/v1/auth/refresh")
                .bodyValue(Map.of("refreshToken", tokens.refreshToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TOKENS)
                .returnResult().getResponseBody();
        assertNotNull(refreshed);
        assertNotNull(refreshed.data());
        assertNotEquals(tokens.refreshToken(), refreshed.data().refreshToken(),
                "le refresh token doit avoir été renouvelé (rotation)");

        // 6) L'ancien refresh token est révoqué -> 401
        client.post().uri("/api/v1/auth/refresh")
                .bodyValue(Map.of("refreshToken", tokens.refreshToken()))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_avec_mauvais_mot_de_passe_renvoie_401() {
        String email = "lea@demo.dev";
        client.post().uri("/api/v1/auth/register")
                .bodyValue(Map.of("email", email, "password", "goodpassword", "displayName", "Léa"))
                .exchange().expectStatus().isCreated();

        client.post().uri("/api/v1/auth/login")
                .bodyValue(Map.of("email", email, "password", "wrongpassword"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error.code").isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    void admin_peut_lister_les_utilisateurs_mais_pas_un_learner() {
        // Un LEARNER fraîchement inscrit
        String learnerEmail = "simple@demo.dev";
        client.post().uri("/api/v1/auth/register")
                .bodyValue(Map.of("email", learnerEmail, "password", "password123", "displayName", "Simple"))
                .exchange().expectStatus().isCreated();

        // Sans jeton -> 401
        client.get().uri("/api/v1/admin/users").exchange().expectStatus().isUnauthorized();

        // Avec un jeton LEARNER -> 403 (interdit)
        TokensResponse learner = login(learnerEmail, "password123");
        client.get().uri("/api/v1/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + learner.accessToken())
                .exchange().expectStatus().isForbidden();

        // Avec un jeton ADMIN (compte seedé) -> 200
        TokensResponse admin = login("admin@it.dev", "Skillforge123!");
        client.get().uri("/api/v1/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin.accessToken())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").value(v -> org.junit.jupiter.api.Assertions
                        .assertTrue(((Number) v).intValue() >= 1));
    }

    @Test
    void admin_change_role_et_desactive_un_utilisateur() {
        String email = "promu@demo.dev";
        ApiResponse<UserProfileResponse> registered = client.post().uri("/api/v1/auth/register")
                .bodyValue(Map.of("email", email, "password", "password123", "displayName", "Promu"))
                .exchange().expectStatus().isCreated()
                .expectBody(new ParameterizedTypeReference<ApiResponse<UserProfileResponse>>() {
                })
                .returnResult().getResponseBody();
        assertNotNull(registered);
        String id = registered.data().id().toString();

        TokensResponse admin = login("admin@it.dev", "Skillforge123!");
        String bearer = "Bearer " + admin.accessToken();

        // Changer les rôles -> INSTRUCTOR
        client.put().uri("/api/v1/admin/users/" + id + "/roles")
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .bodyValue(Map.of("roles", java.util.List.of("INSTRUCTOR")))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.data.roles[0]").isEqualTo("INSTRUCTOR");

        // Désactiver le compte
        client.patch().uri("/api/v1/admin/users/" + id + "/status")
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .bodyValue(Map.of("enabled", false))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.data.enabled").isEqualTo(false);

        // Le compte désactivé ne peut plus se connecter -> 401
        client.post().uri("/api/v1/auth/login")
                .bodyValue(Map.of("email", email, "password", "password123"))
                .exchange().expectStatus().isUnauthorized();
    }

    private TokensResponse login(String email, String password) {
        ApiResponse<TokensResponse> body = client.post().uri("/api/v1/auth/login")
                .bodyValue(Map.of("email", email, "password", password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TOKENS)
                .returnResult().getResponseBody();
        assertNotNull(body);
        return body.data();
    }
}
