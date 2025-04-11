package co.zw.telone.paymentgateway.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends TokenServiceException {
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause, HttpStatus.FORBIDDEN);
    }
}