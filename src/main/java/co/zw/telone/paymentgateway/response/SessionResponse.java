package co.zw.telone.paymentgateway.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private SessionData data;
    private ErrorData error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionData {
        private String id;
        private SessionInfo sessionInfo;
        private String token;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfo {
        private SessionRequest.Amount amount;
        private MerchantInfo merchant;
        private String paymentType;
        private String merchantTransactionId;
        private String description;
        private String status;
        private String returnUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MerchantInfo {
        private String name;
        private String id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorData {
        private String code;
        private String message;
        private String statusCode;
    }
}