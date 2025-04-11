package co.zw.telone.paymentgateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class TokenServiceException extends RuntimeException {
    private final HttpStatus status;

    public TokenServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public TokenServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

}