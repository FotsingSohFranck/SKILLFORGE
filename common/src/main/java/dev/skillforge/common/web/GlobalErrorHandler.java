package dev.skillforge.common.web;

import dev.skillforge.common.api.ApiError;
import dev.skillforge.common.api.ApiResponse;
import dev.skillforge.common.error.BusinessRuleException;
import dev.skillforge.common.error.ConflictException;
import dev.skillforge.common.error.NotFoundException;
import dev.skillforge.common.error.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import java.util.List;

/**
 * Gestion d'erreurs réactive centralisée : traduit les exceptions de domaine et de
 * validation en {@link ApiResponse} normalisées (cf. docs/ARCHITECTURE.md §3.5/§3.6).
 * Aucune stacktrace n'est renvoyée au client.
 */
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.code(), ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.code(), ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, ex.code(), ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.code(), ex.getMessage());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(WebExchangeBindException ex) {
        List<ApiError.FieldError> details = ex.getFieldErrors().stream()
                .map(fe -> new ApiError.FieldError(fe.getField(),
                        fe.getDefaultMessage() == null ? "invalide" : fe.getDefaultMessage()))
                .toList();
        ApiError error = ApiError.of("VALIDATION_ERROR", "Requête invalide.", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadInput(ServerWebInputException ex) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Requête invalide.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String reason = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
        return build(status, status.name(), reason);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Une erreur inattendue est survenue.");
    }

    private ResponseEntity<ApiResponse<Object>> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(ApiResponse.error(ApiError.of(code, message)));
    }
}
