package dev.skillforge.lab.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.lab.adapter.in.web.dto.LabPracticalDto;
import dev.skillforge.lab.adapter.in.web.dto.LabPracticalImportRequest;
import dev.skillforge.lab.adapter.in.web.dto.LabSubmitRequest;
import dev.skillforge.lab.adapter.in.web.dto.LabVerdictDto;
import dev.skillforge.lab.adapter.in.web.dto.TerminalTicketDto;
import dev.skillforge.lab.adapter.in.web.dto.TerminalTicketRequest;
import dev.skillforge.lab.domain.model.LearnerId;
import dev.skillforge.lab.domain.port.in.GetPracticalUseCase;
import dev.skillforge.lab.domain.port.in.ImportPracticalTaskUseCase;
import dev.skillforge.lab.domain.port.in.IssueTerminalTicketUseCase;
import dev.skillforge.lab.domain.port.in.PracticalTaskImport;
import dev.skillforge.lab.domain.port.in.SubmitPracticalUseCase;
import dev.skillforge.lab.domain.port.in.SubmittedFile;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    private final GetPracticalUseCase getPractical;
    private final ImportPracticalTaskUseCase importPractical;
    private final IssueTerminalTicketUseCase issueTerminalTicket;

    public LabController(SubmitPracticalUseCase submitPractical, GetPracticalUseCase getPractical,
                         ImportPracticalTaskUseCase importPractical,
                         IssueTerminalTicketUseCase issueTerminalTicket) {
        this.submitPractical = submitPractical;
        this.getPractical = getPractical;
        this.importPractical = importPractical;
        this.issueTerminalTicket = issueTerminalTicket;
    }

    @GetMapping("/nodes/{nodeId}/practical")
    public Mono<ApiResponse<LabPracticalDto>> getForNode(Principal principal, @PathVariable UUID nodeId) {
        return getPractical.get(learner(principal), nodeId)
                .map(LabPracticalDto::from)
                .map(ApiResponse::ok);
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

    /**
     * Import/mise à jour de l'épreuve pratique d'un nœud (pivot de contenu).
     * Réservé INSTRUCTOR/ADMIN (autorisation appliquée par la SecurityWebFilterChain).
     */
    @PostMapping("/nodes/{nodeId}/practical/import")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> importForNode(
            @PathVariable UUID nodeId, @Valid @RequestBody LabPracticalImportRequest request) {
        PracticalTaskImport command = new PracticalTaskImport(
                nodeId,
                request.title(),
                request.image(),
                request.instructions(),
                request.workspaceSeed(),
                request.validationRef(),
                request.timeoutOrDefault(),
                request.requiresPracticalOrDefault(),
                request.expectedPath());
        return importPractical.importTask(command);
    }

    /**
     * Émet un ticket court (usage unique) pour ouvrir un terminal interactif sur ce nœud.
     * Le JWT ne circule pas dans l'URL du WebSocket : le client présente ce ticket au handshake.
     */
    @PostMapping("/nodes/{nodeId}/terminal/ticket")
    public Mono<ApiResponse<TerminalTicketDto>> terminalTicket(
            Principal principal, @PathVariable UUID nodeId, @RequestBody(required = false) TerminalTicketRequest request) {
        TerminalTicketRequest req = request == null ? new TerminalTicketRequest(null, null) : request;
        return issueTerminalTicket.issue(learner(principal).value(), nodeId, req.colsOrDefault(), req.rowsOrDefault())
                .map(TerminalTicketDto::from)
                .map(ApiResponse::ok);
    }

    private static LearnerId learner(Principal principal) {
        return LearnerId.of(UUID.fromString(principal.getName()));
    }
}
