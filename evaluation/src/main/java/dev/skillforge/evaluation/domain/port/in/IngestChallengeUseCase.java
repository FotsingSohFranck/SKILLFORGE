package dev.skillforge.evaluation.domain.port.in;

import dev.skillforge.evaluation.domain.model.ChallengeId;
import reactor.core.publisher.Mono;

/** Importer (créer/mettre à jour) un défi de code. */
public interface IngestChallengeUseCase {
    Mono<ChallengeId> ingest(ChallengeImport command);
}
