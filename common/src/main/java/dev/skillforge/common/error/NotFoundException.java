package dev.skillforge.common.error;

/** Ressource introuvable → HTTP 404. */
public class NotFoundException extends DomainException {

    public NotFoundException(String code, String message) {
        super(code, message);
    }
}
