package dev.skillforge.contenu.application;

import dev.skillforge.contenu.domain.model.Lesson;
import dev.skillforge.contenu.domain.model.NodeId;
import dev.skillforge.contenu.domain.port.in.ListNodeLessonsUseCase;
import dev.skillforge.contenu.domain.port.out.LessonRepository;
import reactor.core.publisher.Flux;

public class ListNodeLessonsService implements ListNodeLessonsUseCase {

    private final LessonRepository lessons;

    public ListNodeLessonsService(LessonRepository lessons) {
        this.lessons = lessons;
    }

    @Override
    public Flux<Lesson> list(NodeId node) {
        return lessons.findByNodeId(node);
    }
}
