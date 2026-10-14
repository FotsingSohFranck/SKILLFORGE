package dev.skillforge.catalogue.it;

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
 * Test d'intégration de l'ingestion : import d'un pivot roadmap, puis <b>ré-import idempotent</b>
 * (aucun doublon, identifiants préservés) contre PostgreSQL 18 (Testcontainers).
 */
@SpringBootTest(classes = CatalogueITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RoadmapImportIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("skillforge")
                    .withInitScript("db/catalogue-it.sql");

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

    private Map<String, Object> pivot() {
        return Map.of(
                "slug", "import-demo",
                "title", "Parcours importé",
                "kind", "TRACK",
                "position", 10,
                "phases", List.of(Map.of("title", "Base", "position", 0)),
                "nodes", List.of(
                        Map.of("slug", "intro", "title", "Introduction", "phase", "Base",
                                "importance", "CORE", "position", 0),
                        Map.of("slug", "suite", "title", "Suite", "phase", "Base", "position", 1)),
                "edges", List.of(Map.of("source", "intro", "target", "suite", "kind", "PREREQ")));
    }

    @Test
    void import_puis_reimport_est_idempotent() {
        // 1er import -> 201
        client.post().uri("/api/v1/roadmaps/import").bodyValue(pivot())
                .exchange().expectStatus().isCreated()
                .expectBody().jsonPath("$.data.slug").isEqualTo("import-demo");

        // Le graphe est bien créé
        client.get().uri("/api/v1/roadmaps/import-demo")
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.phases.length()").isEqualTo(1)
                .jsonPath("$.data.nodes.length()").isEqualTo(2)
                .jsonPath("$.data.edges.length()").isEqualTo(1);
        String nodeIntroIdAfter1 = nodeId("intro");

        // 2e import du MÊME pivot -> 201, mais aucun doublon
        client.post().uri("/api/v1/roadmaps/import").bodyValue(pivot())
                .exchange().expectStatus().isCreated();

        client.get().uri("/api/v1/roadmaps/import-demo")
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.phases.length()").isEqualTo(1)   // pas de doublon
                .jsonPath("$.data.nodes.length()").isEqualTo(2)
                .jsonPath("$.data.edges.length()").isEqualTo(1);

        // L'identifiant du nœud "intro" est PRÉSERVÉ après ré-import
        String nodeIntroIdAfter2 = nodeId("intro");
        org.junit.jupiter.api.Assertions.assertEquals(nodeIntroIdAfter1, nodeIntroIdAfter2,
                "l'id du nœud doit être préservé (upsert par slug)");
    }

    @Test
    void import_met_a_jour_le_titre_d_un_noeud_existant() {
        client.post().uri("/api/v1/roadmaps/import").bodyValue(pivot())
                .exchange().expectStatus().isCreated();

        // Ré-import avec un titre modifié pour le nœud "intro"
        var updated = new java.util.HashMap<>(pivot());
        updated.put("nodes", List.of(
                Map.of("slug", "intro", "title", "Introduction (v2)", "phase", "Base",
                        "importance", "CORE", "position", 0),
                Map.of("slug", "suite", "title", "Suite", "phase", "Base", "position", 1)));
        client.post().uri("/api/v1/roadmaps/import").bodyValue(updated)
                .exchange().expectStatus().isCreated();

        client.get().uri("/api/v1/roadmaps/import-demo")
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.nodes[?(@.slug=='intro')].title").isEqualTo("Introduction (v2)");
    }

    /** Renvoie l'id du nœud de slug donné via le graphe. */
    private String nodeId(String slug) {
        var body = client.get().uri("/api/v1/roadmaps/import-demo")
                .exchange().expectStatus().isOk()
                .expectBody(new org.springframework.core.ParameterizedTypeReference<
                        Map<String, Object>>() {
                })
                .returnResult().getResponseBody();
        org.junit.jupiter.api.Assertions.assertNotNull(body);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) data.get("nodes");
        return nodes.stream()
                .filter(n -> slug.equals(n.get("slug")))
                .map(n -> String.valueOf(n.get("id")))
                .findFirst().orElseThrow();
    }
}
