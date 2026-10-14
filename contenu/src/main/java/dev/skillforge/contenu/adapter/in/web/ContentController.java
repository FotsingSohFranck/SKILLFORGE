package dev.skillforge.contenu.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.contenu.adapter.in.web.dto.LessonDetailDto;
import dev.skillforge.contenu.adapter.in.web.dto.LessonDto;
import dev.skillforge.contenu.adapter.in.web.mapper.ContentWebMapper;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.NodeId;
import dev.skillforge.contenu.domain.port.in.GetLessonUseCase;
import dev.skillforge.contenu.domain.port.in.ListNodeLessonsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/** Adaptateur entrant WebFlux du contexte Contenu (lecture). */
@RestController
@RequestMapping("/api/v1")
public class ContentController {

    private final ListNodeLessonsUseCase listNodeLessons;
    private final GetLessonUseCase getLesson;

    public ContentController(ListNodeLessonsUseCase listNodeLessons, GetLessonUseCase getLesson) {
        this.listNodeLessons = listNodeLessons;
        this.getLesson = getLesson;
    }

    /** Liste les leçons d'un nœud. */
    @GetMapping("/nodes/{nodeId}/lessons")
    public Mono<ApiResponse<List<LessonDto>>> lessonsOfNode(@PathVariable UUID nodeId) {
        return listNodeLessons.list(new NodeId(nodeId))
                .map(ContentWebMapper::toDto)
                .collectList()
                .map(ApiResponse::ok);
    }

    /** Obtient une leçon et ses sections. */
    @GetMapping("/lessons/{lessonId}")
    public Mono<ApiResponse<LessonDetailDto>> lesson(@PathVariable UUID lessonId) {
        return getLesson.get(new LessonId(lessonId))
                .map(ContentWebMapper::toDto)
                .map(ApiResponse::ok);
    }
}
