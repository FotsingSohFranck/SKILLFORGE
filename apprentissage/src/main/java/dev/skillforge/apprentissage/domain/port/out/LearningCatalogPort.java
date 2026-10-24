package dev.skillforge.apprentissage.domain.port.out;

import dev.skillforge.apprentissage.domain.model.CompletionPolicy;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import reactor.core.publisher.Mono;

/**
 * Port sortant vers le catalogue/contenu : compteurs nécessaires aux pourcentages.
 * <p><b>Couplage temporaire</b> (SF-21) : l'adapter lit directement les tables
 * {@code lessons}/{@code nodes}. À re-router vers un service {@code contenu}/{@code catalogue}
 * en SF-22.
 */
public interface LearningCatalogPort {

    Mono<Long> countLessonsInNode(NodeId node);

    Mono<Long> countNodesInRoadmap(RoadmapId roadmap);

    /** Politique de validation du nœud (défaut LESSONS_ONLY si inconnue). */
    Mono<CompletionPolicy> completionPolicy(NodeId node);

    /** Parcours auquel appartient le nœud (pour résoudre l'inscription depuis un événement). */
    Mono<RoadmapId> roadmapOfNode(NodeId node);
}
