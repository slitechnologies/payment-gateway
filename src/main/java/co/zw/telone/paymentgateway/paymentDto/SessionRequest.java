package co.zw.telone.paymentgateway.paymentDto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequest {
    private Amount amount;
    private Merchant merchant;
    private String paymentType;
    private String merchantTransactionId;
    private String description;
    private String returnUrl;

}