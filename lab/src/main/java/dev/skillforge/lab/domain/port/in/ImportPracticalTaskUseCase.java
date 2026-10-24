package dev.skillforge.lab.domain.port.in;

import reactor.core.publisher.Mono;

/** Crée/met à jour l'épreuve pratique d'un nœud et fixe sa politique de validation. */
public interface ImportPracticalTaskUseCase {

    Mono<Void> importTask(PracticalTaskImport command);
}
