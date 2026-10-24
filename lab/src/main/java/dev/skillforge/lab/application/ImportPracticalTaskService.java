package dev.skillforge.lab.application;

import dev.skillforge.common.id.UuidV7;
import dev.skillforge.lab.domain.model.PracticalTask;
import dev.skillforge.lab.domain.model.PracticalTaskId;
import dev.skillforge.lab.domain.port.in.ImportPracticalTaskUseCase;
import dev.skillforge.lab.domain.port.in.PracticalTaskImport;
import dev.skillforge.lab.domain.port.out.LabCatalogPort;
import dev.skillforge.lab.domain.port.out.PracticalTaskStore;
import reactor.core.publisher.Mono;

/**
 * Importe l'épreuve pratique d'un nœud : upsert de la définition (table du contexte lab),
 * puis mise à jour de la politique de validation du nœud (requise ou non).
 */
public class ImportPracticalTaskService implements ImportPracticalTaskUseCase {

    private final PracticalTaskStore tasks;
    private final LabCatalogPort catalog;

    public ImportPracticalTaskService(PracticalTaskStore tasks, LabCatalogPort catalog) {
        this.tasks = tasks;
        this.catalog = catalog;
    }

    @Override
    public Mono<Void> importTask(PracticalTaskImport command) {
        PracticalTask task = new PracticalTask(
                PracticalTaskId.of(UuidV7.generate()),
                command.nodeId(),
                command.title(),
                command.image(),
                command.instructions(),
                command.workspaceSeed(),
                command.validationRef(),
                command.timeoutSeconds());
        return tasks.upsert(task)
                .then(catalog.setCompletionPolicy(command.nodeId(), command.requiresPractical()));
    }
}
