package dev.skillforge.contenu.adapter.in.web.mapper;

import dev.skillforge.contenu.adapter.in.web.dto.LessonDetailDto;
import dev.skillforge.contenu.adapter.in.web.dto.LessonDto;
import dev.skillforge.contenu.adapter.in.web.dto.SectionDto;
import dev.skillforge.contenu.domain.model.Lesson;
import dev.skillforge.contenu.domain.model.LessonDetail;
import dev.skillforge.contenu.domain.model.Section;

/** Traduction domaine → DTO web (hors du domaine). */
public final class ContentWebMapper {

    private ContentWebMapper() {
    }

    public static LessonDto toDto(Lesson l) {
        return new LessonDto(l.id().value(), l.nodeId().value(), l.slug(), l.title(), l.position());
    }

    public static SectionDto toDto(Section s) {
        return new SectionDto(
                s.id().value(),
                s.type().name(),
                s.position(),
                s.title(),
                s.body(),
                s.codeLanguage(),
                s.diagramFormat() == null ? null : s.diagramFormat().name(),
                s.solution(),
                s.quizId());
    }

    public static LessonDetailDto toDto(LessonDetail d) {
        return new LessonDetailDto(
                toDto(d.lesson()),
                d.sections().stream().map(ContentWebMapper::toDto).toList());
    }
}
