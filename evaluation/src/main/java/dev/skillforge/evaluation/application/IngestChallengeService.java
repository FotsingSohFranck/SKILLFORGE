package dev.skillforge.evaluation.application;

import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.evaluation.domain.model.ChallengeId;
import dev.skillforge.evaluation.domain.port.in.ChallengeImport;
import dev.skillforge.evaluation.domain.port.in.IngestChallengeUseCase;
import dev.skillforge.evaluation.domain.port.out.ChallengeImporter;
import reactor.core.publisher.Mono;

/**
 * Valide le pivot de défi puis délègue la persistance idempotente à {@link ChallengeImporter}.
 * Échec de validation → {@link BusinessRuleException} (code {@code INVALID_CHALLENGE}) → 422.
 */
public class IngestChallengeService implements IngestChallengeUseCase {

    private final ChallengeImporter importer;

    public IngestChallengeService(ChallengeImporter importer) {
        this.importer = importer;
    }

    @Override
    public Mono<ChallengeId> ingest(ChallengeImport command) {
        try {
            validate(command);
        } catch (BusinessRuleException ex) {
            return Mono.error(ex);
        }
        return importer.importChallenge(command);
    }

    private void validate(ChallengeImport c) {
        require(c != null && c.id() != null, "L'identifiant du défi est requis.");
        require(notBlank(c.title()), "Le titre du défi est requis.");
        require(notBlank(c.statement()), "L'énoncé du défi est requis.");
        require(c.keyPoints() != null && !c.keyPoints().isEmpty(),
                "Au moins un point clé de validation est requis.");
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new BusinessRuleException("INVALID_CHALLENGE", message);
        }
    }
}
