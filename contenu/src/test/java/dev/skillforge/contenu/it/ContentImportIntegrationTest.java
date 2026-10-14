package dev.skillforge.contenu.it;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.Map;

/**
 * Test d'intégration de l'ingestion de contenu : import d'un pivot, ré-import idempotent
 * (id de leçon préservé, sections remplacées) contre PostgreSQL 18 (Testcontainers).
 */
@SpringBootTest(classes = ContentITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ContentImportIntegrationTest {

    // Nœud dédié à l'import (distinct du nœud seedé en lecture).
    private static final String NODE = "019b0000-0000-7000-8000-0000000000ff";

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("skillforge")
                    .withInitScript("db/content-it.sql");

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

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    private Map<String, Object> pivot(int sectionCount) {
        var sections = sectionCount == 2
                ? List.of(
                Map.of("type", "CONCEPT", "position", 0, "title", "Notion", "body", "texte"),
                Map.of("type", "CODE", "position", 1, "title", "Exemple", "body", "code", "codeLanguage", "java"))
                : List.of(Map.of("type", "CONCEPT", "position", 0, "title", "Notion", "body", "texte"));
        return Map.of("lessons", List.of(
                Map.of("slug", "intro", "title", "Introduction", "position", 0, "sections", sections)));
    }

    @Test
    void import_puis_reimport_est_idempotent_et_preserve_l_id_de_lecon() {
        // 1er import (2 sections) -> 201, 1 leçon
        client.post().uri("/api/v1/nodes/" + NODE + "/content/import").bodyValue(pivot(2))
                .exchange().expectStatus().isCreated()
                .expectBody().jsonPath("$.data.lessonCount").isEqualTo(1);

        String lessonId1 = firstLessonId();

        // Ré-import (1 section) -> toujours 1 leçon, id préservé, sections remplacées (1)
        client.post().uri("/api/v1/nodes/" + NODE + "/content/import").bodyValue(pivot(1))
                .exchange().expectStatus().isCreated();

        // Toujours une seule leçon pour ce nœud
        client.get().uri("/api/v1/nodes/" + NODE + "/lessons")
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.data.length()").isEqualTo(1);

        String lessonId2 = firstLessonId();
        org.junit.jupiter.api.Assertions.assertEquals(lessonId1, lessonId2,
                "l'id de la leçon doit être préservé (upsert par slug)");

        // Les sections ont été remplacées : il n'en reste qu'une
        client.get().uri("/api/v1/lessons/" + lessonId2)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.data.sections.length()").isEqualTo(1);
    }

    @Test
    void import_invalide_renvoie_422() {
        Map<String, Object> bad = Map.of("lessons", List.of(
                Map.of("slug", "x", "title", "X", "sections",
                        List.of(Map.of("type", "SUPER", "position", 0)))));
        client.post().uri("/api/v1/nodes/" + NODE + "/content/import").bodyValue(bad)
                .exchange().expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.error.code").isEqualTo("INVALID_CONTENT");
    }

    private String firstLessonId() {
        var body = client.get().uri("/api/v1/nodes/" + NODE + "/lessons")
                .exchange().expectStatus().isOk()
                .expectBody(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                })
                .returnResult().getResponseBody();
        org.junit.jupiter.api.Assertions.assertNotNull(body);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
        return String.valueOf(data.get(0).get("id"));
    }
}
