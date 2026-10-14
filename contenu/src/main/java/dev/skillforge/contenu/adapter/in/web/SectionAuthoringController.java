package dev.skillforge.contenu.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.contenu.adapter.in.web.dto.ReorderRequest;
import dev.skillforge.contenu.adapter.in.web.dto.SectionDto;
import dev.skillforge.contenu.adapter.in.web.dto.SectionRequest;
import dev.skillforge.contenu.adapter.in.web.mapper.ContentWebMapper;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.SectionId;
import dev.skillforge.contenu.domain.port.in.SectionAuthoringUseCase;
import dev.skillforge.contenu.domain.port.in.SectionContent;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Authoring des sections (INSTRUCTOR/ADMIN, autorisation appliquée par la SecurityWebFilterChain). */
@RestController
@RequestMapping("/api/v1")
public class SectionAuthoringController {

    private final SectionAuthoringUseCase authoring;

    public SectionAuthoringController(SectionAuthoringUseCase authoring) {
        this.authoring = authoring;
    }

    @PostMapping("/lessons/{lessonId}/sections")
    public Mono<ResponseEntity<ApiResponse<SectionDto>>> create(
            @PathVariable UUID lessonId, @Valid @RequestBody SectionRequest request) {
        return authoring.create(new LessonId(lessonId), toContent(request))
                .map(ContentWebMapper::toDto)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto)));
    }

    @PutMapping("/sections/{sectionId}")
    public Mono<ApiResponse<SectionDto>> update(
            @PathVariable UUID sectionId, @Valid @RequestBody SectionRequest request) {
        return authoring.update(new SectionId(sectionId), toContent(request))
                .map(ContentWebMapper::toDto)
                .map(ApiResponse::ok);
    }

    @DeleteMapping("/sections/{sectionId}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID sectionId) {
        return authoring.delete(new SectionId(sectionId))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PutMapping("/lessons/{lessonId}/sections/order")
    public Mono<ResponseEntity<Void>> reorder(
            @PathVariable UUID lessonId, @Valid @RequestBody ReorderRequest request) {
        return authoring.reorder(new LessonId(lessonId), request.ids().stream().map(SectionId::new).toList())
                .thenReturn(ResponseEntity.noContent().build());
    }

    private static SectionContent toContent(SectionRequest r) {
        return new SectionContent(r.type(), r.position(), r.title(), r.body(),
                r.codeLanguage(), r.diagramFormat(), r.solution(), r.quizId());
    }
}
