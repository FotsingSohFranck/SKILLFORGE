package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.port.in.ChallengeImport;
import dev.skillforge.evaluation.domain.port.out.ChallengeImporter;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

class IngestChallengeServiceTest {

    private final ChallengeImporter importer = c -> Mono.just(new ChallengeId(c.id()));
    private final IngestChallengeService service = new IngestChallengeService(importer);

    @Test
    void import_valide_delegue() {
        ChallengeImport ok = new ChallengeImport(UUID.randomUUID(), null, null, "Défi", "Énoncé",
                "// squelette", "solution", List.of("point"), "java");
        StepVerifier.create(service.ingest(ok)).expectNextCount(1).verifyComplete();
    }

    @Test
    void rejette_sans_point_cle() {
        ChallengeImport bad = new ChallengeImport(UUID.randomUUID(), null, null, "Défi", "Énoncé",
                null, "solution", List.of(), "java");
        StepVerifier.create(service.ingest(bad))
                .expectErrorMatches(e -> e instanceof BusinessRuleException b
                        && b.code().equals("INVALID_CHALLENGE"))
                .verify();
    }
}
