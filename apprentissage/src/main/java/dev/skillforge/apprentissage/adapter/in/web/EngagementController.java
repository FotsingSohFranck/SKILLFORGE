package dev.skillforge.apprentissage.adapter.in.web;

import dev.skillforge.apprentissage.adapter.in.web.dto.BookmarkDto;
import dev.skillforge.apprentissage.adapter.in.web.dto.StreakDto;
import dev.skillforge.apprentissage.adapter.in.web.dto.ToggleBookmarkRequest;
import dev.skillforge.apprentissage.adapter.in.web.dto.ToggleResultDto;
import dev.skillforge.apprentissage.adapter.in.web.mapper.LearningWebMapper;
import dev.skillforge.apprentissage.domain.model.BookmarkTarget;
import dev.skillforge.apprentissage.domain.model.LearnerId;
import dev.skillforge.apprentissage.domain.port.in.GetStreakUseCase;
import dev.skillforge.apprentissage.domain.port.in.ListBookmarksUseCase;
import dev.skillforge.apprentissage.domain.port.in.ToggleBookmarkUseCase;
import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.common.error.BusinessRuleException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** Favoris et série d'apprentissage de l'apprenant courant (principal = userId). */
@RestController
@RequestMapping("/api/v1/learning")
public class EngagementController {

    private final ToggleBookmarkUseCase toggleBookmark;
    private final ListBookmarksUseCase listBookmarks;
    private final GetStreakUseCase getStreak;

    public EngagementController(ToggleBookmarkUseCase toggleBookmark, ListBookmarksUseCase listBookmarks,
                               GetStreakUseCase getStreak) {
        this.toggleBookmark = toggleBookmark;
        this.listBookmarks = listBookmarks;
        this.getStreak = getStreak;
    }

    @PostMapping("/bookmarks")
    public Mono<ApiResponse<ToggleResultDto>> toggle(Principal principal,
                                                     @Valid @RequestBody ToggleBookmarkRequest request) {
        return toggleBookmark.toggle(learner(principal), parseTarget(request.targetType()), request.targetId())
                .map(bookmarked -> ApiResponse.ok(new ToggleResultDto(bookmarked)));
    }

    @GetMapping("/bookmarks")
    public Mono<ApiResponse<List<BookmarkDto>>> list(Principal principal) {
        return listBookmarks.list(learner(principal))
                .map(LearningWebMapper::toDto)
                .collectList()
                .map(ApiResponse::ok);
    }

    @GetMapping("/streak")
    public Mono<ApiResponse<StreakDto>> streak(Principal principal) {
        return getStreak.get(learner(principal))
                .map(LearningWebMapper::toDto)
                .map(ApiResponse::ok);
    }

    private static LearnerId learner(Principal principal) {
        return new LearnerId(UUID.fromString(principal.getName()));
    }

    private static BookmarkTarget parseTarget(String value) {
        try {
            return BookmarkTarget.valueOf(value.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("INVALID_BOOKMARK_TARGET",
                    "Cible de favori invalide : " + value + " (attendu : NODE | LESSON).");
        }
    }
}
