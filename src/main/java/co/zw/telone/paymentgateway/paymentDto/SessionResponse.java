package co.zw.telone.paymentgateway.paymentDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {
    private SessionData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionData {
        private String id;
        private SessionInfo sessionInfo;
        private String token;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionInfo {
        private Amount amount;
        private Merchant merchant;
        private String paymentType;
        private String merchantTransactionId;
        private String description;
        private String status;
        private String returnUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Amount {
        private String amountInCents;
        private String currency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Merchant {
        private String name;
        private String id;
    }
}