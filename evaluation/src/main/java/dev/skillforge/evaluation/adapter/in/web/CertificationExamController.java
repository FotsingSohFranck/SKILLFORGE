package dev.skillforge.evaluation.adapter.in.web;

import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.evaluation.adapter.in.web.dto.ExamSummaryDto;
import dev.skillforge.evaluation.domain.port.in.ListCertificationExamsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/** Découverte des examens blancs d'une certification. */
@RestController
@RequestMapping("/api/v1/certifications")
public class CertificationExamController {

    private final ListCertificationExamsUseCase listExams;

    public CertificationExamController(ListCertificationExamsUseCase listExams) {
        this.listExams = listExams;
    }

    @GetMapping("/{certificationId}/exams")
    public Mono<ApiResponse<List<ExamSummaryDto>>> byCertification(@PathVariable UUID certificationId) {
        return listExams.list(certificationId)
                .map(e -> new ExamSummaryDto(e.id().value(), e.title(), e.format(), e.durationMinutes()))
                .collectList()
                .map(ApiResponse::ok);
    }
}
