package dev.skillforge.lab.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.lab.adapter.in.web.dto.LabSubmitRequest;
import dev.skillforge.lab.adapter.in.web.dto.LabVerdictDto;
import dev.skillforge.lab.domain.model.LearnerId;
import dev.skillforge.lab.domain.port.in.SubmitPracticalUseCase;
import dev.skillforge.lab.domain.port.in.SubmittedFile;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** Épreuve pratique d'un nœud : soumission de fichiers et correction automatique (verdict). */
@RestController
@RequestMapping("/api/v1/lab")
public class LabController {

    private final SubmitPracticalUseCase submitPractical;

    public LabController(SubmitPracticalUseCase submitPractical) {
        this.submitPractical = submitPractical;
    }

    @PostMapping("/nodes/{nodeId}/submit")
    public Mono<ApiResponse<LabVerdictDto>> submit(
            Principal principal, @PathVariable UUID nodeId, @Valid @RequestBody LabSubmitRequest request) {
        List<SubmittedFile> files = request.files().stream()
                .map(f -> new SubmittedFile(f.path(), f.content()))
                .toList();
        return submitPractical.submit(learner(principal), nodeId, files)
                .map(LabVerdictDto::from)
                .map(ApiResponse::ok);
    }

    private static LearnerId learner(Principal principal) {
        return LearnerId.of(UUID.fromString(principal.getName()));
    }
}
