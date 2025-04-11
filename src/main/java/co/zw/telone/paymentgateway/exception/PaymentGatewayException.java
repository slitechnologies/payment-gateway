package co.zw.telone.paymentgateway.exception;


public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message) {
        super(message);
    }

    public PaymentGatewayException(String message, Throwable cause, int code) {
        super(message, cause);
    }
}