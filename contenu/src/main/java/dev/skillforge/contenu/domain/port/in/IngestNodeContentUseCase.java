package dev.skillforge.contenu.domain.port.in;

import reactor.core.publisher.Mono;

/** Importer (créer/mettre à jour) le contenu d'un nœud. Renvoie le nombre de leçons importées. */
public interface IngestNodeContentUseCase {
    Mono<Integer> ingest(NodeContentImport command);
}
