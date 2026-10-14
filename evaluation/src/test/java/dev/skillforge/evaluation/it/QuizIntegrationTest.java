package dev.skillforge.evaluation.it;

import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.model.OptionId;
import dev.skillforge.evaluation.domain.model.QuestionId;
import dev.skillforge.evaluation.domain.model.Quiz;
import dev.skillforge.evaluation.domain.model.QuizId;
import dev.skillforge.evaluation.domain.port.in.IngestQuizUseCase;
import dev.skillforge.evaluation.domain.port.in.QuizImport;
import dev.skillforge.evaluation.domain.port.in.SubmitAttemptUseCase;
import dev.skillforge.evaluation.domain.port.in.SubmittedAnswer;
import dev.skillforge.evaluation.domain.port.out.QuizRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test d'intégration Évaluation : import d'un quiz, lecture (sans révéler les bonnes réponses),
 * puis correction d'une tentative contre PostgreSQL 18 (Testcontainers).
 */
@SpringBootTest(classes = EvaluationITApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class QuizIntegrationTest {

    private static final UUID QUIZ = UUID.fromString("019c0000-0000-7000-8000-000000000021");

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

    @Autowired IngestQuizUseCase ingest;
    @Autowired SubmitAttemptUseCase submit;
    @Autowired QuizRepository quizzes;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        // Import du quiz (2 questions, une option correcte chacune).
        ingest.ingest(new QuizImport(QUIZ, null, null, "Quiz démo", "desc", List.of(
                new QuizImport.QuestionImport("MCQ", "2 + 2 ?", "car 4", 0, List.of(
                        new QuizImport.OptionImport("4", true, 0),
                        new QuizImport.OptionImport("5", false, 1))),
                new QuizImport.QuestionImport("TRUE_FALSE", "Le ciel est bleu ?", "vrai", 1, List.of(
                        new QuizImport.OptionImport("Vrai", true, 0),
                        new QuizImport.OptionImport("Faux", false, 1)))))).block();
    }

    @Test
    void lecture_du_quiz_ne_revele_pas_les_bonnes_reponses() {
        client.get().uri("/api/v1/quizzes/" + QUIZ)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.questions.length()").isEqualTo(2)
                .jsonPath("$.data.questions[0].options.length()").isEqualTo(2)
                // La vue « passage » ne contient ni correct ni justification.
                .jsonPath("$.data.questions[0].options[0].correct").doesNotExist()
                .jsonPath("$.data.questions[0].justification").doesNotExist();
    }

    @Test
    void tentative_corrige_et_persiste_le_score() {
        Quiz quiz = quizzes.findById(new QuizId(QUIZ)).block();
        QuestionId q1 = quiz.questions().get(0).id();
        OptionId q1Correct = quiz.questions().get(0).correctOption().id();
        QuestionId q2 = quiz.questions().get(1).id();
        OptionId q2Wrong = quiz.questions().get(1).options().stream()
                .filter(o -> !o.correct()).findFirst().orElseThrow().id();

        var result = submit.submit(new LearnerId(UUID.randomUUID()), new QuizId(QUIZ),
                List.of(new SubmittedAnswer(q1, q1Correct), new SubmittedAnswer(q2, q2Wrong))).block();

        assertEquals(1, result.score());
        assertEquals(2, result.maxScore());
    }
}
