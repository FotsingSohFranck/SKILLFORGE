package dev.skillforge.apprentissage.adapter.in.web;

import dev.skillforge.apprentissage.adapter.in.web.dto.ActivityRequest;
import dev.skillforge.apprentissage.adapter.in.web.dto.EnrollRequest;
import dev.skillforge.apprentissage.adapter.in.web.dto.EnrollmentDto;
import dev.skillforge.apprentissage.adapter.in.web.dto.NodeLessonProgressDto;
import dev.skillforge.apprentissage.adapter.in.web.dto.ResumeDto;
import dev.skillforge.apprentissage.adapter.in.web.dto.RoadmapProgressDto;
import dev.skillforge.apprentissage.adapter.in.web.mapper.LearningWebMapper;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.port.in.CompleteLessonUseCase;
import dev.skillforge.apprentissage.domain.port.in.EnrollLearnerUseCase;
import dev.skillforge.apprentissage.domain.port.in.GetNodeLessonProgressUseCase;
import dev.skillforge.apprentissage.domain.port.in.GetRoadmapProgressUseCase;
import dev.skillforge.apprentissage.domain.port.in.ListEnrollmentsUseCase;
import dev.skillforge.apprentissage.domain.port.in.MarkLessonViewedUseCase;
import dev.skillforge.apprentissage.domain.port.in.ResumeLastUseCase;
import dev.skillforge.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * Adaptateur entrant WebFlux du contexte Apprentissage. L'apprenant courant est déduit du
 * jeton (principal = userId), sans dépendance à la couche sécurité.
 */
@RestController
@RequestMapping("/api/v1/learning")
public class LearningController {

    private final EnrollLearnerUseCase enroll;
    private final MarkLessonViewedUseCase markViewed;
    private final CompleteLessonUseCase complete;
    private final GetRoadmapProgressUseCase getProgress;
    private final GetNodeLessonProgressUseCase getNodeLessons;
    private final ListEnrollmentsUseCase listEnrollments;
    private final ResumeLastUseCase resumeLast;

    public LearningController(EnrollLearnerUseCase enroll, MarkLessonViewedUseCase markViewed,
                             CompleteLessonUseCase complete, GetRoadmapProgressUseCase getProgress,
                             GetNodeLessonProgressUseCase getNodeLessons, ListEnrollmentsUseCase listEnrollments,
                             ResumeLastUseCase resumeLast) {
        this.enroll = enroll;
        this.markViewed = markViewed;
        this.complete = complete;
        this.getProgress = getProgress;
        this.getNodeLessons = getNodeLessons;
        this.listEnrollments = listEnrollments;
        this.resumeLast = resumeLast;
    }

    @PostMapping("/enrollments")
    public Mono<ResponseEntity<ApiResponse<EnrollmentDto>>> enroll(
            Principal principal, @Valid @RequestBody EnrollRequest request) {
        return enroll.enroll(learner(principal), new RoadmapId(request.roadmapId()))
                .map(LearningWebMapper::toDto)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto)));
    }

    @GetMapping("/enrollments")
    public Mono<ApiResponse<List<EnrollmentDto>>> enrollments(Principal principal) {
        return listEnrollments.list(learner(principal))
                .map(LearningWebMapper::toDto)
                .collectList()
                .map(ApiResponse::ok);
    }

    @PostMapping("/viewed")
    public Mono<ResponseEntity<Void>> viewed(Principal principal, @Valid @RequestBody ActivityRequest r) {
        return markViewed.markViewed(learner(principal), new RoadmapId(r.roadmapId()),
                        new NodeId(r.nodeId()), new LessonId(r.lessonId()))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping("/completed")
    public Mono<ResponseEntity<Void>> completed(Principal principal, @Valid @RequestBody ActivityRequest r) {
        return complete.complete(learner(principal), new RoadmapId(r.roadmapId()),
                        new NodeId(r.nodeId()), new LessonId(r.lessonId()))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/roadmaps/{roadmapId}/progress")
    public Mono<ApiResponse<RoadmapProgressDto>> progress(Principal principal, @PathVariable UUID roadmapId) {
        return getProgress.get(learner(principal), new RoadmapId(roadmapId))
                .map(LearningWebMapper::toDto)
                .map(ApiResponse::ok);
    }

    @GetMapping("/nodes/{nodeId}/lessons/progress")
    public Mono<ApiResponse<NodeLessonProgressDto>> nodeLessonProgress(
            Principal principal, @PathVariable UUID nodeId) {
        return getNodeLessons.completedLessons(learner(principal), new NodeId(nodeId))
                .map(LessonId::value)
                .collectList()
                .map(ids -> ApiResponse.ok(new NodeLessonProgressDto(ids)));
    }

    @GetMapping("/resume")
    public Mono<ApiResponse<ResumeDto>> resume(Principal principal) {
        return resumeLast.resume(learner(principal))
                .map(LearningWebMapper::toDto)
                .map(ApiResponse::ok)
                .defaultIfEmpty(ApiResponse.ok(null));
    }

    private static LearnerId learner(Principal principal) {
        return new LearnerId(UUID.fromString(principal.getName()));
    }
}
