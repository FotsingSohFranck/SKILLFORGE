package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.ExamItemKind;
import dev.skillforge.evaluation.domain.port.in.ExamImport;
import dev.skillforge.evaluation.domain.port.in.IngestExamUseCase;
import dev.skillforge.evaluation.domain.port.out.ExamImporter;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Valide le pivot d'examen puis délègue la persistance idempotente à {@link ExamImporter}.
 * Échec de validation → {@link BusinessRuleException} (code {@code INVALID_EXAM}) → 422.
 */
public class IngestExamService implements IngestExamUseCase {

    private final ExamImporter importer;

    public IngestExamService(ExamImporter importer) {
        this.importer = importer;
    }

    @Override
    public Mono<ExamId> ingest(ExamImport command) {
        try {
            validate(command);
        } catch (BusinessRuleException ex) {
            return Mono.error(ex);
        }
        return importer.importExam(command);
    }

    private void validate(ExamImport c) {
        require(c != null && c.id() != null, "L'identifiant de l'examen est requis.");
        require(notBlank(c.title()), "Le titre de l'examen est requis.");
        require(c.durationMinutes() != null && c.durationMinutes() > 0, "La durée (minutes) doit être positive.");
        require(!safe(c.items()).isEmpty(), "L'examen doit comporter au moins un item.");
        for (ExamImport.ItemImport item : safe(c.items())) {
            require(item.examDomainId() != null, "Chaque item doit référencer un domaine d'examen.");
            ExamItemKind kind = parseKind(item.kind());
            require(item.points() != null && item.points() > 0, "Chaque item doit avoir des points positifs.");
            if (kind == ExamItemKind.MCQ) {
                require(item.questionId() != null, "Un item MCQ doit référencer une question (questionId).");
            } else {
                require(notBlank(item.prompt()), "Un item TASK doit avoir un énoncé (prompt).");
            }
        }
    }

    private static ExamItemKind parseKind(String kind) {
        try {
            return ExamItemKind.valueOf(kind == null ? "" : kind.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("INVALID_EXAM", "Type d'item invalide : '" + kind + "' (MCQ/TASK).");
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
            throw new BusinessRuleException("INVALID_EXAM", message);
        }
    }
}
