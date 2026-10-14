package dev.skillforge.common.error;

/** Authentification requise ou identifiants invalides → HTTP 401. */
public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String code, String message) {
        super(code, message);
    }
}
