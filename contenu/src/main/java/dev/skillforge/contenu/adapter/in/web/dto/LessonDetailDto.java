package dev.skillforge.contenu.adapter.in.web.dto;

import java.util.List;

/** Représentation web d'une leçon avec ses sections. */
public record LessonDetailDto(
        LessonDto lesson,
        List<SectionDto> sections) {
}
