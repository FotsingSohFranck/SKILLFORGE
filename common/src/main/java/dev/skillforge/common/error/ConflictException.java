package dev.skillforge.common.error;

/** Conflit d'état (ex. doublon) → HTTP 409. */
public class ConflictException extends DomainException {

    public ConflictException(String code, String message) {
        super(code, message);
    }
}
