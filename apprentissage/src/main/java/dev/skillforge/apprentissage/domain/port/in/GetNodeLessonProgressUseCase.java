package dev.skillforge.apprentissage.domain.port.in;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import reactor.core.publisher.Flux;

/** Cas d'usage : leçons complétées par l'apprenant dans un nœud (pour les coches de la vue nœud). */
public interface GetNodeLessonProgressUseCase {

    Flux<LessonId> completedLessons(LearnerId learner, NodeId node);
}
