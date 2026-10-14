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

/**
 * Test d'intégration du contexte Catalogue : flux réel WebFlux → use case → adapter R2DBC
 * → PostgreSQL 18 (Testcontainers). Valide le mapping R2DBC et l'assemblage du graphe.
 */
@SpringBootTest(classes = CatalogueITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CatalogueIntegrationTest {

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
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void liste_les_parcours() {
        client.get().uri("/api/v1/roadmaps")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].slug").isEqualTo("it-backend")
                .jsonPath("$.data[0].kind").isEqualTo("TRACK");
    }

    @Test
    void obtient_un_parcours_avec_son_graphe() {
        client.get().uri("/api/v1/roadmaps/it-backend")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.roadmap.title").isEqualTo("IT Backend")
                .jsonPath("$.data.phases.length()").isEqualTo(1)
                .jsonPath("$.data.nodes.length()").isEqualTo(2)
                .jsonPath("$.data.edges.length()").isEqualTo(1)
                .jsonPath("$.data.edges[0].kind").isEqualTo("PREREQ");
    }

    @Test
    void obtient_un_noeud_par_id() {
        client.get().uri("/api/v1/nodes/01930000-0000-7000-8000-0000000000d1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.slug").isEqualTo("internet")
                .jsonPath("$.data.importance").isEqualTo("CORE")
                .jsonPath("$.data.estimatedMinutes").isEqualTo(60);
    }

    @Test
    void obtient_la_certification_d_un_parcours() {
        client.get().uri("/api/v1/roadmaps/ckad/certification")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.code").isEqualTo("CKAD")
                .jsonPath("$.data.examFormat").isEqualTo("PRACTICAL")
                .jsonPath("$.data.durationMinutes").isEqualTo(120)
                .jsonPath("$.data.domains.length()").isEqualTo(2)
                .jsonPath("$.data.domains[0].name").isEqualTo("Application Design");
    }

    @Test
    void obtient_une_certification_par_id() {
        client.get().uri("/api/v1/certifications/01930000-0000-7000-8000-0000000000f1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.code").isEqualTo("CKAD")
                .jsonPath("$.data.domains.length()").isEqualTo(2);
    }

    @Test
    void renvoie_404_si_le_parcours_n_est_pas_une_certification() {
        client.get().uri("/api/v1/roadmaps/it-backend/certification")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error.code").isEqualTo("CERTIFICATION_NOT_FOUND");
    }

    @Test
    void renvoie_404_normalise_pour_un_parcours_inconnu() {
        client.get().uri("/api/v1/roadmaps/inexistant")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error.code").isEqualTo("ROADMAP_NOT_FOUND");
    }
}
