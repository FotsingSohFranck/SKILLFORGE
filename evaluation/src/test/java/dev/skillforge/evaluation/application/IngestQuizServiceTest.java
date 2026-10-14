package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.evaluation.domain.model.QuizId;
import dev.skillforge.evaluation.domain.port.in.QuizImport;
import dev.skillforge.evaluation.domain.port.out.QuizImporter;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

class IngestQuizServiceTest {

    private final QuizImporter importer = command -> Mono.just(new QuizId(command.id()));
    private final IngestQuizService service = new IngestQuizService(importer);

    private QuizImport valid() {
        return new QuizImport(UUID.randomUUID(), null, null, "Quiz", null, List.of(
                new QuizImport.QuestionImport("MCQ", "Q1", "justif", 0, List.of(
                        new QuizImport.OptionImport("A", true, 0),
                        new QuizImport.OptionImport("B", false, 1)))));
    }

    @Test
    void import_valide_delegue() {
        StepVerifier.create(service.ingest(valid())).expectNextCount(1).verifyComplete();
    }

    @Test
    void rejette_quiz_sans_question() {
        QuizImport bad = new QuizImport(UUID.randomUUID(), null, null, "Quiz", null, List.of());
        expectInvalid(bad);
    }

    @Test
    void rejette_question_sans_option_correcte() {
        QuizImport bad = new QuizImport(UUID.randomUUID(), null, null, "Quiz", null, List.of(
                new QuizImport.QuestionImport("MCQ", "Q", null, 0, List.of(
                        new QuizImport.OptionImport("A", false, 0),
                        new QuizImport.OptionImport("B", false, 1)))));
        expectInvalid(bad);
    }

    @Test
    void rejette_type_de_question_invalide() {
        QuizImport bad = new QuizImport(UUID.randomUUID(), null, null, "Quiz", null, List.of(
                new QuizImport.QuestionImport("SUPER", "Q", null, 0, List.of(
                        new QuizImport.OptionImport("A", true, 0),
                        new QuizImport.OptionImport("B", false, 1)))));
        expectInvalid(bad);
    }

    private void expectInvalid(QuizImport bad) {
        StepVerifier.create(service.ingest(bad))
                .expectErrorMatches(e -> e instanceof BusinessRuleException b
                        && b.code().equals("INVALID_QUIZ"))
                .verify();
    }
}
