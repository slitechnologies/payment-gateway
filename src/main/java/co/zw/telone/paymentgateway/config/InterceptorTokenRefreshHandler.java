package co.zw.telone.paymentgateway.config;

import co.zw.telone.paymentgateway.client.TokenApiClient;
import co.zw.telone.paymentgateway.tokendto.TokenRequest;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class InterceptorTokenRefreshHandler {

    @Value("${payment.gateway.client-id}")
    private String clientId;

    @Value("${payment.gateway.client-secret}")
    private String clientSecret;

    private final TokenApiClient tokenApiClient;

    private String authToken;
    private Long tokenExpiryTime;

    /**
     * Creates a Feign RequestInterceptor to add the Authorization header to outgoing requests.
     *
     * @return RequestInterceptor
     */
    @Bean
    public RequestInterceptor authorizationInterceptor() {
        return template -> {
            // Ensure the token is valid before making any request
            ensureValidToken();

            if (authToken != null) {
                template.header("Authorization", "Bearer " + authToken);
            } else {
                log.warn("Authorization header is missing; Bearer token is null.");
            }
        };
    }

    /**
     * Ensures a valid token exists by refreshing it if necessary.
     */
    public synchronized void ensureValidToken() {
        if (authToken == null || isTokenExpired()) {
            refreshToken();
        }
    }

    /**
     * Checks whether the current token is expired.
     *
     * @return true if the token is expired, false otherwise
     */
    private boolean isTokenExpired() {
        return tokenExpiryTime == null || Instant.now().getEpochSecond() >= (tokenExpiryTime - 60);
    }

    /**
     * Refreshes the authentication token by calling the Token API.
     */
    private void refreshToken() {
        TokenRequest tokenRequest = new TokenRequest(clientId, clientSecret);

        try {
            // Call the Token API to get a new token
            log.info("Fetching a new token from the Token API.");
            Map<String, Object> response = tokenApiClient.getToken(tokenRequest);

            // Validate response map before accessing fields
            if (response == null || !response.containsKey("data")) {
                log.error("Invalid token response from API: {}", response);
                throw new RuntimeException("Token API response is missing required fields.");
            }

            // Extract the token from the "data" object
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data == null || !data.containsKey("token")) {
                log.error("Invalid token data: {}", data);
                throw new RuntimeException("Token API response does not contain a valid token.");
            }

            this.authToken = (String) data.get("token");

            // Fallback for missing expires_in field
            Integer expiresIn = 3600; // Default to 1 hour if not provided
            if (data.containsKey("expires_in")) {
                try {
                    expiresIn = Integer.parseInt(data.get("expires_in").toString());
                } catch (NumberFormatException e) {
                    log.warn("Unable to parse 'expires_in' value. Defaulting to 1 hour.");
                }
            } else {
                log.warn("Token expiry time not provided by API. Defaulting to 1 hour.");
            }

            // Calculate expiry time
            this.tokenExpiryTime = Instant.now().getEpochSecond() + expiresIn;

            log.info("Token refreshed successfully. Expires at: {}", tokenExpiryTime);

        } catch (Exception e) {
            log.error("Error while refreshing token.", e);
            throw new RuntimeException("Failed to refresh authentication token.", e);
        }
    }

    /**
     * Returns the current authentication token.
     *
     * @return authToken
     */
    public String getToken() {
        if (authToken == null || isTokenExpired()) {
            log.warn("Token is invalid or expired. Refreshing token...");
            ensureValidToken();
        }
        return authToken;
    }
}













//package co.zw.telone.paymentgateway.config;
//
//import co.zw.telone.paymentgateway.client.TokenApiClient;
//import co.zw.telone.paymentgateway.tokendto.TokenRequest;
//import feign.RequestInterceptor;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.time.Instant;
//import java.util.Map;
//
//@Configuration
//@RequiredArgsConstructor
//@Slf4j
//public class InterceptorTokenRefreshHandler {
//
//    @Value("${payment.gateway.client-id}")
//    private String clientId;
//
//    @Value("${payment.gateway.client-secret}")
//    private String clientSecret;
//
//    private final TokenApiClient tokenApiClient;
//
//    private String authToken;
//    private Long tokenExpiryTime;
//
//    /**
//     * Creates a Feign RequestInterceptor to add the Authorization header to outgoing requests.
//     *
//     * @return RequestInterceptor
//     */
//    @Bean
//    public RequestInterceptor authorizationInterceptor() {
//        return template -> {
//            // Ensure the token is valid before making any request
//            ensureValidToken();
//
//            if (authToken != null) {
//                template.header("Authorization", "Bearer " + authToken);
//            } else {
//                log.warn("Authorization header is missing; Bearer token is null");
//            }
//        };
//    }
//
//    /**
//     * Ensures a valid token exists by refreshing it if necessary.
//     */
//    public synchronized void ensureValidToken() {
//        if (authToken == null || isTokenExpired()) {
//            refreshToken();
//        }
//    }
//
//    /**
//     * Checks whether the current token is expired.
//     *
//     * @return true if the token is expired, false otherwise
//     */
//    private boolean isTokenExpired() {
//        return tokenExpiryTime == null || Instant.now().getEpochSecond() >= (tokenExpiryTime - 60);
//    }
//
//    /**
//     * Refreshes the authentication token by calling the Token API.
//     */
//    private void refreshToken() {
//        TokenRequest tokenRequest = new TokenRequest(clientId, clientSecret);
//
//        try {
//            // Call the Token API to get a new token
//            Map<String, Object> response = tokenApiClient.getToken(tokenRequest);
//
//            // Validate response map before accessing fields
//            if (response == null || !response.containsKey("data")) {
//                log.error("Invalid token response from API: {}", response);
//                throw new RuntimeException("Token API response is missing required fields");
//            }
//
//            // Extract the token from the "data" object
//            Map<String, Object> data = (Map<String, Object>) response.get("data");
//            if (data == null || !data.containsKey("token")) {
//                log.error("Invalid token data: {}", data);
//                throw new RuntimeException("Token API response does not contain a valid token");
//            }
//
//            this.authToken = (String) data.get("token");
//
//            // Fallback for missing expires_in field
//            Integer expiresIn = 3600; // Default to 1 hour if not provided
//            if (data.containsKey("expires_in")) {
//                expiresIn = (Integer) data.get("expires_in");
//            } else {
//                log.warn("Token expiry time not provided by API. Defaulting to 1 hour.");
//            }
//
//            // Calculate expiry time
//            this.tokenExpiryTime = Instant.now().getEpochSecond() + expiresIn;
//
//            log.info("Token refreshed successfully. Expires at: {}", tokenExpiryTime);
//        } catch (Exception e) {
//            log.error("Error while refreshing token", e);
//            throw new RuntimeException("Failed to refresh authentication token", e);
//        }
//    }
//
//    public String getToken() {
//        return null;
//    }
//}