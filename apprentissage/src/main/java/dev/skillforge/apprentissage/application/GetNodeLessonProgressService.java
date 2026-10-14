package dev.skillforge.apprentissage.application;

import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.port.in.GetNodeLessonProgressUseCase;
import dev.skillforge.apprentissage.domain.port.out.LessonProgressStore;
import reactor.core.publisher.Flux;

/** Retourne les leçons complétées d'un nœud pour l'apprenant courant. */
public class GetNodeLessonProgressService implements GetNodeLessonProgressUseCase {

    private final LessonProgressStore lessonProgress;

    public GetNodeLessonProgressService(LessonProgressStore lessonProgress) {
        this.lessonProgress = lessonProgress;
    }

    @Override
    public Flux<LessonId> completedLessons(LearnerId learner, NodeId node) {
        return lessonProgress.findCompletedLessonIds(learner, node);
    }
}
