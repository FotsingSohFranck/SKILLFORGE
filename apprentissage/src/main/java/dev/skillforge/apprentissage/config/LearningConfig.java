package dev.skillforge.apprentissage.config;

import dev.skillforge.apprentissage.application.CompleteLessonService;
import dev.skillforge.apprentissage.application.EnrollLearnerService;
import dev.skillforge.apprentissage.application.GetNodeLessonProgressService;
import dev.skillforge.apprentissage.application.GetRoadmapProgressService;
import dev.skillforge.apprentissage.application.GetStreakService;
import dev.skillforge.apprentissage.application.ListBookmarksService;
import dev.skillforge.apprentissage.application.ListEnrollmentsService;
import dev.skillforge.apprentissage.application.MarkLessonViewedService;
import dev.skillforge.apprentissage.application.ProgressRecalculator;
import dev.skillforge.apprentissage.application.ResumeLastService;
import dev.skillforge.apprentissage.application.StreakUpdater;
import dev.skillforge.apprentissage.application.ToggleBookmarkService;
import dev.skillforge.apprentissage.domain.port.in.CompleteLessonUseCase;
import dev.skillforge.apprentissage.domain.port.in.EnrollLearnerUseCase;
import dev.skillforge.apprentissage.domain.port.in.GetNodeLessonProgressUseCase;
import dev.skillforge.apprentissage.domain.port.in.GetRoadmapProgressUseCase;
import dev.skillforge.apprentissage.domain.port.in.GetStreakUseCase;
import dev.skillforge.apprentissage.domain.port.in.ListBookmarksUseCase;
import dev.skillforge.apprentissage.domain.port.in.ListEnrollmentsUseCase;
import dev.skillforge.apprentissage.domain.port.in.MarkLessonViewedUseCase;
import dev.skillforge.apprentissage.domain.port.in.ResumeLastUseCase;
import dev.skillforge.apprentissage.domain.port.in.ToggleBookmarkUseCase;
import dev.skillforge.apprentissage.domain.port.out.BookmarkStore;
import dev.skillforge.apprentissage.domain.port.out.EnrollmentStore;
import dev.skillforge.apprentissage.domain.port.out.LearningCatalogPort;
import dev.skillforge.apprentissage.domain.port.out.LearningEventPublisher;
import dev.skillforge.apprentissage.domain.port.out.LessonProgressStore;
import dev.skillforge.apprentissage.domain.port.out.NodeProgressStore;
import dev.skillforge.apprentissage.domain.port.out.StreakStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Câblage du contexte Apprentissage : services applicatifs purs exposés en beans. */
@Configuration
public class LearningConfig {

    @Bean
    ProgressRecalculator progressRecalculator(EnrollmentStore enrollments, NodeProgressStore nodeProgress,
                                              LessonProgressStore lessonProgress, LearningCatalogPort catalog) {
        return new ProgressRecalculator(enrollments, nodeProgress, lessonProgress, catalog);
    }

    @Bean
    StreakUpdater streakUpdater(StreakStore streaks, LearningEventPublisher events) {
        return new StreakUpdater(streaks, events);
    }

    @Bean
    EnrollLearnerUseCase enrollLearnerUseCase(EnrollmentStore enrollments) {
        return new EnrollLearnerService(enrollments);
    }

    @Bean
    MarkLessonViewedUseCase markLessonViewedUseCase(EnrollmentStore enrollments,
                                                    LessonProgressStore lessonProgress,
                                                    ProgressRecalculator recalculator,
                                                    StreakUpdater streakUpdater) {
        return new MarkLessonViewedService(enrollments, lessonProgress, recalculator, streakUpdater);
    }

    @Bean
    CompleteLessonUseCase completeLessonUseCase(EnrollmentStore enrollments,
                                                LessonProgressStore lessonProgress,
                                                ProgressRecalculator recalculator,
                                                StreakUpdater streakUpdater,
                                                LearningEventPublisher events) {
        return new CompleteLessonService(enrollments, lessonProgress, recalculator, streakUpdater, events);
    }

    @Bean
    GetRoadmapProgressUseCase getRoadmapProgressUseCase(EnrollmentStore enrollments,
                                                        NodeProgressStore nodeProgress) {
        return new GetRoadmapProgressService(enrollments, nodeProgress);
    }

    @Bean
    GetNodeLessonProgressUseCase getNodeLessonProgressUseCase(LessonProgressStore lessonProgress) {
        return new GetNodeLessonProgressService(lessonProgress);
    }

    @Bean
    ListEnrollmentsUseCase listEnrollmentsUseCase(EnrollmentStore enrollments) {
        return new ListEnrollmentsService(enrollments);
    }

    @Bean
    ResumeLastUseCase resumeLastUseCase(EnrollmentStore enrollments) {
        return new ResumeLastService(enrollments);
    }

    @Bean
    ToggleBookmarkUseCase toggleBookmarkUseCase(BookmarkStore bookmarks) {
        return new ToggleBookmarkService(bookmarks);
    }

    @Bean
    ListBookmarksUseCase listBookmarksUseCase(BookmarkStore bookmarks) {
        return new ListBookmarksService(bookmarks);
    }

    @Bean
    GetStreakUseCase getStreakUseCase(StreakStore streaks) {
        return new GetStreakService(streaks);
    }
}
