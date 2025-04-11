package co.zw.telone.paymentgateway.paymentDto;



import lombok.Data;

@Data
public class PaymentCallbackResponse {
    private boolean success;
    private String message;
    private String transactionId;
}