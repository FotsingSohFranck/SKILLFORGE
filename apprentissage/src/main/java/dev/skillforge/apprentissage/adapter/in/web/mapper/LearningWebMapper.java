package dev.skillforge.apprentissage.adapter.in.web.mapper;

import dev.skillforge.apprentissage.adapter.in.web.dto.BookmarkDto;
import dev.skillforge.apprentissage.adapter.in.web.dto.EnrollmentDto;
import dev.skillforge.apprentissage.adapter.in.web.dto.NodeProgressDto;
import dev.skillforge.apprentissage.adapter.in.web.dto.ResumeDto;
import dev.skillforge.apprentissage.adapter.in.web.dto.RoadmapProgressDto;
import dev.skillforge.apprentissage.adapter.in.web.dto.StreakDto;
import dev.skillforge.apprentissage.domain.model.Bookmark;
import dev.skillforge.apprentissage.domain.model.Enrollment;
import dev.skillforge.apprentissage.domain.model.LessonId;
import dev.skillforge.apprentissage.domain.model.NodeId;
import dev.skillforge.apprentissage.domain.model.NodeProgress;
import dev.skillforge.apprentissage.domain.model.ResumePointer;
import dev.skillforge.apprentissage.domain.model.RoadmapId;
import dev.skillforge.apprentissage.domain.model.RoadmapProgress;
import dev.skillforge.apprentissage.domain.model.Streak;

import java.util.UUID;

/** Traduction domaine → DTO web (hors du domaine). */
public final class LearningWebMapper {

    private LearningWebMapper() {
    }

    public static EnrollmentDto toDto(Enrollment e) {
        return new EnrollmentDto(
                e.id().value(),
                e.roadmapId().value(),
                e.progressPercent(),
                unwrap(e.lastActiveNodeId()),
                unwrap(e.lastActiveLessonId()));
    }

    public static NodeProgressDto toDto(NodeProgress n) {
        return new NodeProgressDto(
                n.nodeId().value(),
                n.status().name(),
                n.percent(),
                unwrap(n.lastLessonId()));
    }

    public static RoadmapProgressDto toDto(RoadmapProgress p) {
        return new RoadmapProgressDto(
                toDto(p.enrollment()),
                p.nodes().stream().map(LearningWebMapper::toDto).toList());
    }

    public static ResumeDto toDto(ResumePointer r) {
        return new ResumeDto(unwrap(r.roadmapId()), unwrap(r.nodeId()), unwrap(r.lessonId()));
    }

    public static BookmarkDto toDto(Bookmark b) {
        return new BookmarkDto(b.targetType().name(), b.targetId());
    }

    public static StreakDto toDto(Streak s) {
        return new StreakDto(s.currentLength(), s.longestLength(), s.lastActivityDate());
    }

    private static UUID unwrap(RoadmapId id) {
        return id == null ? null : id.value();
    }

    private static UUID unwrap(NodeId id) {
        return id == null ? null : id.value();
    }

    private static UUID unwrap(LessonId id) {
        return id == null ? null : id.value();
    }
}
