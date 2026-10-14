package dev.skillforge.evaluation.it;

import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.port.in.ChallengeImport;
import dev.skillforge.evaluation.domain.port.in.IngestChallengeUseCase;
import dev.skillforge.evaluation.domain.port.in.SubmitChallengeUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test d'intégration des défis de code : import (jsonb points clés), lecture (sans solution),
 * validation d'une soumission contre PostgreSQL 18 (Testcontainers).
 */
@SpringBootTest(classes = EvaluationITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ChallengeIntegrationTest {

    private static final UUID CHALLENGE = UUID.fromString("019c0000-0000-7000-8000-0000000000c1");
    private static final UUID NODE = UUID.fromString("019c0000-0000-7000-8000-0000000000d1");
    private static final UUID CHALLENGE_ON_NODE = UUID.fromString("019c0000-0000-7000-8000-0000000000c2");

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("skillforge")
                    .withInitScript("db/evaluation-it.sql");

    @DynamicPropertySource
    static void r2dbcProps(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
                + POSTGRES.getHost() + ":" + POSTGRES.getFirstMappedPort()
                + "/" + POSTGRES.getDatabaseName());
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
    }

    @Value("${local.server.port}")
    int port;

    @Autowired IngestChallengeUseCase ingest;
    @Autowired SubmitChallengeUseCase submit;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        ingest.ingest(new ChallengeImport(CHALLENGE, null, null, "Cache en mémoire",
                "Implémente un cache thread-safe.", "// TODO", "Map cache = new ConcurrentHashMap();",
                List.of("ConcurrentHashMap", "computeIfAbsent"), "java")).block();
    }

    @Test
    void lecture_du_defi_ne_revele_pas_la_solution() {
        client.get().uri("/api/v1/challenges/" + CHALLENGE)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.title").isEqualTo("Cache en mémoire")
                .jsonPath("$.data.language").isEqualTo("java")
                .jsonPath("$.data.solution").doesNotExist()
                .jsonPath("$.data.keyPoints").doesNotExist();
    }

    @Test
    void liste_les_defis_d_un_noeud() {
        ingest.ingest(new ChallengeImport(CHALLENGE_ON_NODE, NODE, null, "Défi du nœud",
                "Énoncé.", "// TODO", "solution", List.of("kp"), "java")).block();

        client.get().uri("/api/v1/nodes/" + NODE + "/challenges")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].id").isEqualTo(CHALLENGE_ON_NODE.toString())
                .jsonPath("$.data[0].title").isEqualTo("Défi du nœud")
                .jsonPath("$.data[0].language").isEqualTo("java");
    }

    @Test
    void soumission_valide_par_points_cles() {
        // Réussite : les deux points clés sont présents.
        var ok = submit.submit(new ChallengeId(CHALLENGE),
                "var c = new ConcurrentHashMap<>(); c.computeIfAbsent(k, f);").block();
        assertTrue(ok.passed());
        assertTrue(ok.missing().isEmpty());

        // Échec : un point clé manque.
        var ko = submit.submit(new ChallengeId(CHALLENGE), "var c = new HashMap<>();").block();
        assertFalse(ko.passed());
        assertFalse(ko.missing().isEmpty());
    }
}
