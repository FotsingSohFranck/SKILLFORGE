package dev.skillforge.iam.domain.model;

import dev.skillforge.common.error.BusinessRuleException;

import java.util.regex.Pattern;

/**
 * Adresse e-mail (value object) : normalisée en minuscules et validée à la construction.
 * Invariant du domaine — la validation fine du format se fait aussi au bord (DTO) en SF-12b.
 */
public record Email(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("INVALID_EMAIL", "L'adresse e-mail est requise.");
        }
        value = value.trim().toLowerCase();
        if (!PATTERN.matcher(value).matches()) {
            throw new BusinessRuleException("INVALID_EMAIL", "Adresse e-mail invalide : " + value);
        }
    }
}
