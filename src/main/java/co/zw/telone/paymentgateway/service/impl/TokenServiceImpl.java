package co.zw.telone.paymentgateway.service.impl;

import co.zw.telone.paymentgateway.client.TokenApiClient;
import co.zw.telone.paymentgateway.tokendto.TokenRequest;
import co.zw.telone.paymentgateway.tokendto.TokenResponse;
import co.zw.telone.paymentgateway.exception.GeneralTokenException;
import co.zw.telone.paymentgateway.exception.InvalidCredentialsException;
import co.zw.telone.paymentgateway.exception.PaymentGatewayConnectionException;
import co.zw.telone.paymentgateway.exception.TokenNotFoundException;
import co.zw.telone.paymentgateway.service.TokenService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final TokenApiClient tokenApiClient;

    @Value("${payment.gateway.client-id}")
    private String clientId;

    @Value("${payment.gateway.client-secret}")
    private String clientSecret;

    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

    /**
     * Gets an authentication token from the payment gateway, with caching.
     *
     * @return TokenResponse containing the token.
     */
    @Override
    public TokenResponse getAuthenticationToken() {
        String cachedToken = tokenCache.get("authToken");

        if (cachedToken != null) {
            log.info("Returning cached token");
            return new TokenResponse(cachedToken);
        }

        TokenRequest request = TokenRequest.builder()
                .username(clientId)
                .password(clientSecret)
                .build();

        try {
            Map<String, Object> response = tokenApiClient.getToken(request);

            // Extract the token from the response
            Optional<String> token = extractTokenFromResponse(response);

            if (token.isPresent()) {
                String authToken = token.get();
                tokenCache.put("authToken", authToken); // Cache the token
                log.info("Successfully fetched and cached token.");
                return new TokenResponse(authToken);
            }

            log.error("Unable to extract token from response: {}", response);
            throw new TokenNotFoundException("Token not found in response");

        } catch (FeignException.Forbidden e) {
            log.error("Authentication failed with status 403: {}", e.getMessage());
            throw new InvalidCredentialsException("Invalid credentials", e);
        } catch (FeignException e) {
            log.error("Failed to get token: {} - {}", e.status(), e.getMessage());
            throw new PaymentGatewayConnectionException("Failed to connect to payment gateway", e);
        } catch (Exception e) {
            log.error("Unexpected error while getting token", e);
            throw new GeneralTokenException("An unexpected error occurred", e);
        }
    }

    /**
     * Extract token from the nested response structure.
     *
     * @param response The API response.
     * @return Optional containing the token if present.
     */
    private Optional<String> extractTokenFromResponse(Map<String, Object> response) {
        try {
            return Optional.ofNullable(response.get("data"))
                    .filter(data -> data instanceof Map)
                    .map(data -> (Map<String, Object>) data)
                    .map(data -> data.get("token"))
                    .filter(token -> token instanceof String)
                    .map(token -> (String) token);

        } catch (ClassCastException | NullPointerException e) {
            log.error("Failed to parse response: {}", response, e);
            return Optional.empty();
        }
    }
}