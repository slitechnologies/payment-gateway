package co.zw.telone.paymentgateway.exception;



import org.springframework.http.HttpStatus;

public class GeneralTokenException extends TokenServiceException {
    public GeneralTokenException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}