package co.zw.telone.paymentgateway.paymentDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSessionResponse {
    private String paymentSessionId;
    private Amount amount;
    private Merchant merchant;
    private String paymentType;
    private String merchantTransactionId;
    private String description;
    private String returnUrl;
    private String paymentUrl;
    private String status;


//    curl --location 'https://pg.telpay.youcloudtech.com/api/v1/sessions' \
//--data '{
//    "amount": {
//        "amountInCents": "2500",
//        "currency": "ZWL"
//    },
//    "merchant": {
//        "name": "Merchant Name"
//    },
//    "paymentType": "PURCHASE",
//    "merchantTransactionId": "unique-request-id-16112023-3",
//    "description": "test SALE payment",
//    "returnUrl" : "https://test.com"
//}'
}
