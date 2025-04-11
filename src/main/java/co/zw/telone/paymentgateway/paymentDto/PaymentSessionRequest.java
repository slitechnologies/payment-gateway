package co.zw.telone.paymentgateway.paymentDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSessionRequest {

    private Amount amount;
    private Merchant merchant;
    private String paymentType;
    private String merchantTransactionId;
    private String description;
    private String returnUrl;

}
