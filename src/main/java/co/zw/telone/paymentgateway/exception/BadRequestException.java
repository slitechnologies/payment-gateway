package co.zw.telone.paymentgateway.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends TokenServiceException {
    public BadRequestException(String message, HttpStatus status) {
        super(message, status);
    }

    public BadRequestException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }
}
