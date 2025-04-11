package co.zw.telone.paymentgateway.response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Amount {
        private String amountInCents;
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Merchant {
        private String name;
    }
}