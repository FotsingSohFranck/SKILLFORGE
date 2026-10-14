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

/**
 * Test d'intégration de la lecture du contenu contre PostgreSQL 18 (Testcontainers) :
 * liste des leçons d'un nœud + leçon avec ses sections typées.
 */
@SpringBootTest(classes = ContentITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ContentIntegrationTest {

    private static final String NODE = "019b0000-0000-7000-8000-0000000000b1";
    private static final String LESSON = "019b0000-0000-7000-8000-0000000000c1";

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

    @Test
    void liste_les_lecons_d_un_noeud() {
        client.get().uri("/api/v1/nodes/" + NODE + "/lessons")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].slug").isEqualTo("intro");
    }

    @Test
    void obtient_une_lecon_avec_ses_sections_typees() {
        client.get().uri("/api/v1/lessons/" + LESSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.lesson.title").isEqualTo("Introduction")
                .jsonPath("$.data.sections.length()").isEqualTo(3)
                .jsonPath("$.data.sections[0].type").isEqualTo("CONCEPT")
                .jsonPath("$.data.sections[1].type").isEqualTo("CODE")
                .jsonPath("$.data.sections[1].codeLanguage").isEqualTo("java")
                .jsonPath("$.data.sections[2].type").isEqualTo("QUIZ")
                .jsonPath("$.data.sections[2].quizId").isEqualTo("019b0000-0000-7000-8000-000000000021");
    }

    @Test
    void lecon_inconnue_renvoie_404() {
        client.get().uri("/api/v1/lessons/00000000-0000-7000-8000-000000000000")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error.code").isEqualTo("LESSON_NOT_FOUND");
    }

    @Test
    void authoring_crud_complet_lecon_et_section() {
        String node = "019b0000-0000-7000-8000-0000000000aa"; // nœud dédié à l'authoring

        // Créer une leçon -> 201
        String lessonId = dataId(client.post().uri("/api/v1/nodes/" + node + "/lessons")
                .bodyValue(java.util.Map.of("slug", "nouvelle", "title", "Nouvelle leçon", "position", 0))
                .exchange().expectStatus().isCreated());

        // Éditer la leçon -> 200
        client.put().uri("/api/v1/lessons/" + lessonId)
                .bodyValue(java.util.Map.of("slug", "nouvelle", "title", "Leçon éditée", "position", 1))
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.data.title").isEqualTo("Leçon éditée");

        // Créer une section -> 201
        String sectionId = dataId(client.post().uri("/api/v1/lessons/" + lessonId + "/sections")
                .bodyValue(java.util.Map.of("type", "CONCEPT", "position", 0, "title", "Intro", "body", "texte"))
                .exchange().expectStatus().isCreated());

        // La leçon a 1 section
        client.get().uri("/api/v1/lessons/" + lessonId)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.data.sections.length()").isEqualTo(1);

        // Supprimer la section -> 204 ; la leçon n'a plus de section
        client.delete().uri("/api/v1/sections/" + sectionId).exchange().expectStatus().isNoContent();
        client.get().uri("/api/v1/lessons/" + lessonId)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.data.sections.length()").isEqualTo(0);

        // Supprimer la leçon -> 204 ; elle devient introuvable
        client.delete().uri("/api/v1/lessons/" + lessonId).exchange().expectStatus().isNoContent();
        client.get().uri("/api/v1/lessons/" + lessonId).exchange().expectStatus().isNotFound();
    }

    @Test
    void supprimer_une_lecon_inconnue_renvoie_404() {
        client.delete().uri("/api/v1/lessons/00000000-0000-7000-8000-000000000000")
                .exchange().expectStatus().isNotFound();
    }

    /** Extrait {@code data.id} du corps d'une réponse (leçon ou section créée). */
    @SuppressWarnings("unchecked")
    private String dataId(WebTestClient.ResponseSpec response) {
        var body = response.expectBody(
                        new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {
                        })
                .returnResult().getResponseBody();
        org.junit.jupiter.api.Assertions.assertNotNull(body);
        var data = (java.util.Map<String, Object>) body.get("data");
        return String.valueOf(data.get("id"));
    }
}
