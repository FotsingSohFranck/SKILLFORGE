package dev.skillforge.contenu.adapter.out.persistence.mapper;

import dev.skillforge.contenu.adapter.out.persistence.entity.LessonEntity;
import dev.skillforge.contenu.adapter.out.persistence.entity.SectionEntity;
import dev.skillforge.contenu.domain.model.DiagramFormat;
import dev.skillforge.contenu.domain.model.Lesson;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.NodeId;
import dev.skillforge.contenu.domain.model.Section;
import dev.skillforge.contenu.domain.model.SectionId;
import dev.skillforge.contenu.domain.model.SectionType;

/** Traduction entité de persistance → modèle de domaine (hors du domaine). */
public final class ContentPersistenceMapper {

    private ContentPersistenceMapper() {
    }

    public static Lesson toDomain(LessonEntity e) {
        return new Lesson(
                new LessonId(e.id()),
                new NodeId(e.nodeId()),
                e.slug(),
                e.title(),
                e.position() == null ? 0 : e.position());
    }

    public static Section toDomain(SectionEntity e) {
        return new Section(
                new SectionId(e.id()),
                SectionType.valueOf(e.type()),
                e.position() == null ? 0 : e.position(),
                e.title(),
                e.body(),
                e.codeLanguage(),
                e.diagramFormat() == null ? null : DiagramFormat.valueOf(e.diagramFormat()),
                e.solution(),
                e.quizId());
    }
}
