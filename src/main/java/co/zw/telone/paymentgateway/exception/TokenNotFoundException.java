package co.zw.telone.paymentgateway.exception;

import org.springframework.http.HttpStatus;

public class TokenNotFoundException extends TokenServiceException {
    public TokenNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}