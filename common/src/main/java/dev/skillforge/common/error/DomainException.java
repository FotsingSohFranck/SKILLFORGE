package dev.skillforge.common.error;

/**
 * Exception de domaine : porte un {@code code} métier stable, traduit en code HTTP par
 * le handler d'erreurs global (cf. {@code GlobalErrorHandler}). Le domaine reste pur :
 * ces exceptions ne dépendent ni de Spring, ni du web.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
