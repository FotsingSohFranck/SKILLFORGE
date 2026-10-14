package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.port.in.ExamImport;
import dev.skillforge.evaluation.domain.port.out.ExamImporter;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

class IngestExamServiceTest {

    private final ExamImporter importer = c -> Mono.just(new ExamId(c.id()));
    private final IngestExamService service = new IngestExamService(importer);

    @Test
    void import_valide_delegue() {
        ExamImport ok = new ExamImport(UUID.randomUUID(), UUID.randomUUID(), "CKAD", "PRACTICAL", 120, 66.0,
                List.of(new ExamImport.ItemImport(UUID.randomUUID(), "TASK", null, "Tâche", "sol", 1.0, 0)));
        StepVerifier.create(service.ingest(ok)).expectNextCount(1).verifyComplete();
    }

    @Test
    void rejette_sans_item() {
        ExamImport bad = new ExamImport(UUID.randomUUID(), UUID.randomUUID(), "CKAD", "PRACTICAL", 120, 66.0, List.of());
        expectInvalid(bad);
    }

    @Test
    void rejette_tache_sans_enonce() {
        ExamImport bad = new ExamImport(UUID.randomUUID(), UUID.randomUUID(), "CKAD", "PRACTICAL", 120, 66.0,
                List.of(new ExamImport.ItemImport(UUID.randomUUID(), "TASK", null, " ", "sol", 1.0, 0)));
        expectInvalid(bad);
    }

    @Test
    void rejette_mcq_sans_question() {
        ExamImport bad = new ExamImport(UUID.randomUUID(), UUID.randomUUID(), "KCNA", "MCQ", 90, 75.0,
                List.of(new ExamImport.ItemImport(UUID.randomUUID(), "MCQ", null, null, null, 1.0, 0)));
        expectInvalid(bad);
    }

    private void expectInvalid(ExamImport bad) {
        StepVerifier.create(service.ingest(bad))
                .expectErrorMatches(e -> e instanceof BusinessRuleException b
                        && b.code().equals("INVALID_EXAM"))
                .verify();
    }
}
