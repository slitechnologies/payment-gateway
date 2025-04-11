package co.zw.telone.paymentgateway.exception;

import org.springframework.http.HttpStatus;

public class PaymentGatewayConnectionException extends TokenServiceException {
    public PaymentGatewayConnectionException(String message, Throwable cause) {
        super(message, cause, HttpStatus.SERVICE_UNAVAILABLE);
    }
}