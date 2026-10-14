package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.evaluation.domain.model.QuestionType;
import dev.skillforge.evaluation.domain.model.QuizId;
import dev.skillforge.evaluation.domain.port.in.IngestQuizUseCase;
import dev.skillforge.evaluation.domain.port.in.QuizImport;
import dev.skillforge.evaluation.domain.port.out.QuizImporter;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Valide le pivot de quiz puis délègue la persistance idempotente à {@link QuizImporter}.
 * Échec de validation → {@link BusinessRuleException} (code {@code INVALID_QUIZ}) → HTTP 422.
 */
public class IngestQuizService implements IngestQuizUseCase {

    private final QuizImporter importer;

    public IngestQuizService(QuizImporter importer) {
        this.importer = importer;
    }

    @Override
    public Mono<QuizId> ingest(QuizImport command) {
        try {
            validate(command);
        } catch (BusinessRuleException ex) {
            return Mono.error(ex);
        }
        return importer.importQuiz(command);
    }

    private void validate(QuizImport c) {
        require(c != null && c.id() != null, "L'identifiant du quiz est requis (cohérent avec la section).");
        require(notBlank(c.title()), "Le titre du quiz est requis.");
        require(!safe(c.questions()).isEmpty(), "Le quiz doit comporter au moins une question.");
        for (QuizImport.QuestionImport question : safe(c.questions())) {
            require(notBlank(question.prompt()), "Chaque question doit avoir un énoncé.");
            parseType(question.type());
            List<QuizImport.OptionImport> options = safe(question.options());
            require(options.size() >= 2, "Chaque question doit avoir au moins deux options.");
            long correct = options.stream().filter(o -> Boolean.TRUE.equals(o.correct())).count();
            require(correct == 1, "Chaque question doit avoir exactement une option correcte.");
        }
    }

    private static void parseType(String type) {
        try {
            QuestionType.valueOf(type == null ? "" : type.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("INVALID_QUIZ",
                    "Type de question invalide : '" + type + "' (MCQ/TRUE_FALSE/SCENARIO).");
        }
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new BusinessRuleException("INVALID_QUIZ", message);
        }
    }
}
