package dev.skillforge.common.error;

/** Violation d'une règle métier → HTTP 422. */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String code, String message) {
        super(code, message);
    }
}
