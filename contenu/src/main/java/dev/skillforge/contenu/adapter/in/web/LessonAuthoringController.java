package dev.skillforge.contenu.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.contenu.adapter.in.web.dto.CreateLessonRequest;
import dev.skillforge.contenu.adapter.in.web.dto.LessonDto;
import dev.skillforge.contenu.adapter.in.web.dto.ReorderRequest;
import dev.skillforge.contenu.adapter.in.web.dto.UpdateLessonRequest;
import dev.skillforge.contenu.adapter.in.web.mapper.ContentWebMapper;
import dev.skillforge.contenu.domain.model.LessonId;
import dev.skillforge.contenu.domain.model.NodeId;
import dev.skillforge.contenu.domain.port.in.LessonAuthoringUseCase;
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

/** Authoring des leçons (INSTRUCTOR/ADMIN, autorisation appliquée par la SecurityWebFilterChain). */
@RestController
@RequestMapping("/api/v1")
public class LessonAuthoringController {

    private final LessonAuthoringUseCase authoring;

    public LessonAuthoringController(LessonAuthoringUseCase authoring) {
        this.authoring = authoring;
    }

    @PostMapping("/nodes/{nodeId}/lessons")
    public Mono<ResponseEntity<ApiResponse<LessonDto>>> create(
            @PathVariable UUID nodeId, @Valid @RequestBody CreateLessonRequest request) {
        return authoring.create(new NodeId(nodeId), request.slug(), request.title(), request.position())
                .map(ContentWebMapper::toDto)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto)));
    }

    @PutMapping("/lessons/{lessonId}")
    public Mono<ApiResponse<LessonDto>> update(
            @PathVariable UUID lessonId, @Valid @RequestBody UpdateLessonRequest request) {
        return authoring.update(new LessonId(lessonId), request.slug(), request.title(), request.position())
                .map(ContentWebMapper::toDto)
                .map(ApiResponse::ok);
    }

    @DeleteMapping("/lessons/{lessonId}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID lessonId) {
        return authoring.delete(new LessonId(lessonId))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PutMapping("/nodes/{nodeId}/lessons/order")
    public Mono<ResponseEntity<Void>> reorder(
            @PathVariable UUID nodeId, @Valid @RequestBody ReorderRequest request) {
        return authoring.reorder(new NodeId(nodeId), request.ids().stream().map(LessonId::new).toList())
                .thenReturn(ResponseEntity.noContent().build());
    }
}
