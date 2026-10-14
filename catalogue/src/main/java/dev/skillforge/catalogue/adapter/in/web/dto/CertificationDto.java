package dev.skillforge.catalogue.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

/** Représentation web d'une certification et de ses domaines. */
public record CertificationDto(
        UUID id,
        String code,
        String examFormat,
        int durationMinutes,
        Double passingScore,
        List<ExamDomainDto> domains) {

    public record ExamDomainDto(UUID id, String name, double weight, int position) {
    }
}
