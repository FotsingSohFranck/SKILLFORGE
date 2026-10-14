package dev.skillforge.evaluation.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.evaluation.adapter.in.web.dto.DomainResultDto;
import dev.skillforge.evaluation.adapter.in.web.dto.ExamAttemptDto;
import dev.skillforge.evaluation.adapter.in.web.dto.ExamDto;
import dev.skillforge.evaluation.adapter.in.web.dto.ExamResultDto;
import dev.skillforge.evaluation.adapter.in.web.dto.ExamSubmitRequest;
import dev.skillforge.evaluation.adapter.in.web.mapper.EvaluationWebMapper;
import dev.skillforge.evaluation.domain.model.ExamAttemptId;
import dev.skillforge.evaluation.domain.model.ExamId;
import dev.skillforge.evaluation.domain.model.ExamItemId;
import dev.skillforge.evaluation.domain.model.LearnerId;
import dev.skillforge.evaluation.domain.model.OptionId;
import dev.skillforge.evaluation.domain.port.in.ExamItemAnswer;
import dev.skillforge.evaluation.domain.port.in.GetExamHistoryUseCase;
import dev.skillforge.evaluation.domain.port.in.GetExamUseCase;
import dev.skillforge.evaluation.domain.port.in.GetRevisionUseCase;
import dev.skillforge.evaluation.domain.port.in.StartExamUseCase;
import dev.skillforge.evaluation.domain.port.in.SubmitExamUseCase;
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

/** Examens blancs chronométrés : passage, démarrage, soumission, historique, révision ciblée. */
@RestController
@RequestMapping("/api/v1/exams")
public class ExamController {

    private final GetExamUseCase getExam;
    private final StartExamUseCase startExam;
    private final SubmitExamUseCase submitExam;
    private final GetExamHistoryUseCase history;
    private final GetRevisionUseCase revision;

    public ExamController(GetExamUseCase getExam, StartExamUseCase startExam, SubmitExamUseCase submitExam,
                         GetExamHistoryUseCase history, GetRevisionUseCase revision) {
        this.getExam = getExam;
        this.startExam = startExam;
        this.submitExam = submitExam;
        this.history = history;
        this.revision = revision;
    }

    @GetMapping("/{examId}")
    public Mono<ApiResponse<ExamDto>> get(@PathVariable UUID examId) {
        return getExam.get(new ExamId(examId)).map(EvaluationWebMapper::toTakingDto).map(ApiResponse::ok);
    }

    @PostMapping("/{examId}/attempts")
    public Mono<ResponseEntity<ApiResponse<ExamAttemptDto>>> start(Principal principal, @PathVariable UUID examId) {
        return startExam.start(learner(principal), new ExamId(examId))
                .map(EvaluationWebMapper::toDto)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto)));
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public Mono<ApiResponse<ExamResultDto>> submit(
            Principal principal, @PathVariable UUID attemptId, @Valid @RequestBody ExamSubmitRequest request) {
        List<ExamItemAnswer> answers = request.answers().stream()
                .map(a -> new ExamItemAnswer(new ExamItemId(a.itemId()),
                        a.selectedOptionId() == null ? null : new OptionId(a.selectedOptionId()),
                        a.completed()))
                .toList();
        return submitExam.submit(learner(principal), new ExamAttemptId(attemptId), answers)
                .map(EvaluationWebMapper::toDto)
                .map(ApiResponse::ok);
    }

    @GetMapping("/{examId}/attempts")
    public Mono<ApiResponse<List<ExamAttemptDto>>> history(Principal principal, @PathVariable UUID examId) {
        return history.history(learner(principal), new ExamId(examId))
                .map(EvaluationWebMapper::toDto).collectList().map(ApiResponse::ok);
    }

    @GetMapping("/{examId}/revision")
    public Mono<ApiResponse<List<DomainResultDto>>> revision(Principal principal, @PathVariable UUID examId) {
        return revision.weakDomains(learner(principal), new ExamId(examId))
                .map(EvaluationWebMapper::toDto).collectList().map(ApiResponse::ok);
    }

    private static LearnerId learner(Principal principal) {
        return new LearnerId(UUID.fromString(principal.getName()));
    }
}
