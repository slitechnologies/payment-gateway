package co.zw.telone.paymentgateway.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSessionRequest {
    private String amountInCents;
    private String currency;
    private String merchantName;
    private String merchantTransactionId;
    private String description;
    private String returnUrl;
}