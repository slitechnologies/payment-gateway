package co.zw.telone.paymentgateway.paymentDto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSessionRequest_ {
    private String amountInCents;
    private String currency;
    private String merchantName;
    private String merchantTransactionId;
    private String description;
    private String returnUrl;
}
