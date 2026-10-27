package com.landgo.coreservice.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    /**
     * Machine-readable context for the failure, surfaced as the response's {@code details}.
     *
     * <p>Null for most errors, where the code and message are the whole story. A client that has to
     * correct the request needs the specifics: which field was wrong, what was sent, and what would
     * have been accepted.
     */
    private final Object details;

    public ApiException(String message, HttpStatus status, String errorCode) {
        this(message, status, errorCode, null);
    }

    public ApiException(String message, HttpStatus status, String errorCode, Object details) {
        super(message); this.status = status; this.errorCode = errorCode; this.details = details;
    }
}
