package co.zw.telone.paymentgateway.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSessionResponse {
    private String sessionId;
    private String paymentUrl;
    private String status;
    private String merchantTransactionId;
    private PaymentAmount amount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentAmount {
        private String amountInCents;
        private String currency;
    }
}