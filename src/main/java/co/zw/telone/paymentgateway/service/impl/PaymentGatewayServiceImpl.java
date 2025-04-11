package co.zw.telone.paymentgateway.service.impl;

import co.zw.telone.paymentgateway.client.SessionApiClient;
import co.zw.telone.paymentgateway.config.InterceptorTokenRefreshHandler;
import co.zw.telone.paymentgateway.exception.PaymentGatewayException;
import co.zw.telone.paymentgateway.response.*;
import co.zw.telone.paymentgateway.service.PaymentGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static co.zw.telone.paymentgateway.util.PaymentGatewayConstants.PAYMENT_SESSION_FAILED;
import static co.zw.telone.paymentgateway.util.PaymentGatewayConstants.PAYMENT_TYPE_PURCHASE;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private final SessionApiClient sessionApiClient;
    private final InterceptorTokenRefreshHandler tokenHandler;
    private final RedisUniqueIdService redisUniqueIdService;
    private final ObjectMapper objectMapper;

    @Value("${payment.gateway.payment-page-base-url}")
    private String paymentPageBaseUrl;

    private final AtomicInteger sequentialCounter = new AtomicInteger(0);

    @Override
    public PaymentSessionResponse createPaymentSession(PaymentSessionRequest request) {
        log.info("Creating payment session for merchantTransactionId: {}", request.getMerchantTransactionId());

        String merchantTransactionId = getOrGenerateMerchantTransactionId(request);

        try {
            String token = validateToken();
            String authHeader = buildAuthorizationHeader(token);

            SessionRequest sessionRequest = mapToSessionRequest(request, merchantTransactionId);
            Map<String, Object> responseMap = sessionApiClient.createSession(authHeader, sessionRequest);
            log.debug("Session API response: {}", responseMap);

            SessionResponse gatewayResponse = parseGatewayResponse(responseMap);
            String sessionId = getSessionId(gatewayResponse);
            String paymentPageUrl = buildPaymentPageUrl(sessionId);

            return preparePaymentSessionResponse(sessionId, paymentPageUrl, gatewayResponse.getData().getSessionInfo().getStatus(), request);

        } catch (FeignException.BadRequest ex) {
            handleBadRequest(ex);
        } catch (Exception e) {
            handleUnexpectedError(e, merchantTransactionId);
        }

        return null;
    }

    private String getOrGenerateMerchantTransactionId(PaymentSessionRequest request) {
        String merchantTransactionId = request.getMerchantTransactionId();
        if (merchantTransactionId == null || merchantTransactionId.trim().isEmpty()) {
            merchantTransactionId = redisUniqueIdService.generateUniqueTransactionId(request.getMerchantName());
            log.info("Generated MerchantTransactionId using RedisUniqueIdService: {}", merchantTransactionId);
            request.setMerchantTransactionId(merchantTransactionId);
        }
        return merchantTransactionId;
    }

    private SessionRequest mapToSessionRequest(PaymentSessionRequest request, String merchantTransactionId) {
        return SessionRequest.builder()
                .amount(SessionRequest.Amount.builder()
                        .amountInCents(request.getAmountInCents())
                        .currency(request.getCurrency())
                        .build())
                .merchant(SessionRequest.Merchant.builder()
                        .name(request.getMerchantName())
                        .build())
                .paymentType(PAYMENT_TYPE_PURCHASE)
                .merchantTransactionId(merchantTransactionId)
                .description(request.getDescription())
                .returnUrl(request.getReturnUrl())
                .build();
    }

    private String validateToken() {
        tokenHandler.ensureValidToken();
        String token = tokenHandler.getToken();
        if (token == null) {
            throw new PaymentGatewayException("Failed to retrieve a valid token.");
        }
        return token;
    }

    private String buildAuthorizationHeader(String token) {
        return "Bearer " + token;
    }

    private SessionResponse parseGatewayResponse(Map<String, Object> responseMap) {
        SessionResponse gatewayResponse = objectMapper.convertValue(responseMap, SessionResponse.class);
        if (gatewayResponse.getError() != null) {
            log.error("Error from payment gateway: {}", gatewayResponse.getError().getMessage());
            throw new PaymentGatewayException("Payment gateway error: " + gatewayResponse.getError().getMessage());
        }
        return gatewayResponse;
    }

    private String getSessionId(SessionResponse gatewayResponse) {
        if (gatewayResponse.getData() == null || gatewayResponse.getData().getId() == null) {
            throw new PaymentGatewayException("Session ID is missing in the API response.");
        }
        return gatewayResponse.getData().getId();
    }

    private String buildPaymentPageUrl(String sessionId) {
        return paymentPageBaseUrl + "/" + sessionId;
    }

    private PaymentSessionResponse preparePaymentSessionResponse(String sessionId, String paymentPageUrl, String status, PaymentSessionRequest request) {
        return PaymentSessionResponse.builder()
                .sessionId(sessionId)
                .paymentUrl(paymentPageUrl)
                .status(status)
                .merchantTransactionId(request.getMerchantTransactionId())
                .amount(PaymentSessionResponse.PaymentAmount.builder()
                        .amountInCents(request.getAmountInCents())
                        .currency(request.getCurrency())
                        .build())
                .build();
    }

    private void handleBadRequest(FeignException.BadRequest ex) {
        log.error("Bad Request error during session creation. Response Body: {}", ex.contentUTF8());
        throw new PaymentGatewayException("Invalid request: " + extractErrorMessage(ex), ex, 400);
    }

    private void handleUnexpectedError(Exception e, String merchantTransactionId) {
        log.error("Unexpected error while creating payment session for merchantTransactionId: {}", merchantTransactionId, e);
        throw new PaymentGatewayException(PAYMENT_SESSION_FAILED, e, 500);
    }

    private String extractErrorMessage(FeignException.BadRequest ex) {
        try {
            Map<String, Object> response = objectMapper.readValue(ex.contentUTF8(), Map.class);
            if (response.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                return error.containsKey("message") ? error.get("message").toString() : "Unknown error";
            }
        } catch (Exception parseException) {
            log.warn("Failed to parse error message from response: {}", ex.contentUTF8(), parseException);
        }
        return "An unknown error occurred.";
    }
}











//package co.zw.telone.paymentgateway.service.impl;
//
//import co.zw.telone.paymentgateway.client.SessionApiClient;
//import co.zw.telone.paymentgateway.config.InterceptorTokenRefreshHandler;
//import co.zw.telone.paymentgateway.exception.PaymentGatewayException;
//import co.zw.telone.paymentgateway.response.PaymentSessionRequest;
//import co.zw.telone.paymentgateway.response.PaymentSessionResponse;
//import co.zw.telone.paymentgateway.response.SessionRequest;
//import co.zw.telone.paymentgateway.response.SessionResponse;
//import co.zw.telone.paymentgateway.service.PaymentGatewayService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import feign.FeignException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static co.zw.telone.paymentgateway.util.PaymentGatewayConstants.PAYMENT_SESSION_FAILED;
//import static co.zw.telone.paymentgateway.util.PaymentGatewayConstants.PAYMENT_TYPE_PURCHASE;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentGatewayServiceImpl implements PaymentGatewayService {
//
//    private final SessionApiClient sessionApiClient;
//    private final InterceptorTokenRefreshHandler tokenHandler;
//    private final ObjectMapper objectMapper;
//
//    @Value("${payment.gateway.payment-page-base-url}")
//    private String paymentPageBaseUrl;
//
//    // Atomic counter for sequential numbering
//    private final AtomicInteger sequentialCounter = new AtomicInteger(0);
//
//@Override
//public PaymentSessionResponse createPaymentSession(PaymentSessionRequest request) {
//    log.info("Creating payment session for merchantTransactionId: {}", request.getMerchantTransactionId());
//
//    try {
//        // Ensure a valid token
//        String token = getValidToken();
//        String authHeader = buildAuthorizationHeader(token);
//
//        // Map the incoming request to the session request format expected by the API
//        SessionRequest sessionRequest = mapToSessionRequest(request);
//
//        // Call the session API
//        Map<String, Object> responseMap = sessionApiClient.createSession(authHeader, sessionRequest);
//        log.debug("Session API response: {}", responseMap);
//
//        // Parse the API response
//        SessionResponse gatewayResponse = parseGatewayResponse(responseMap);
//
//        // Construct the payment page URL using the session ID
//        String sessionId = extractSessionId(gatewayResponse);
//        String paymentPageUrl = constructPaymentPageUrl(sessionId);
//
//        // Build and return the response DTO
//        return buildPaymentSessionResponse(sessionId, paymentPageUrl, gatewayResponse.getData().getSessionInfo().getStatus(), request);
//
//    } catch (FeignException.BadRequest ex) {
//        log.error("Bad Request error during session creation. Response Body: {}", ex.contentUTF8());
//        throw new PaymentGatewayException("Invalid request: " + extractErrorMessage(ex), ex, 400);
//    } catch (Exception e) {
//        log.error("Unexpected error while creating payment session for merchantTransactionId: {}", request.getMerchantTransactionId(), e);
//        throw new PaymentGatewayException(PAYMENT_SESSION_FAILED, e, 500);
//    }
//}
//
//    private String extractErrorMessage(FeignException.BadRequest ex) {
//        // Attempt to parse the error message from the FeignException response
//        try {
//            Map<String, Object> response = objectMapper.readValue(ex.contentUTF8(), Map.class);
//            if (response.containsKey("error")) {
//                Map<String, Object> error = (Map<String, Object>) response.get("error");
//                return error.containsKey("message") ? error.get("message").toString() : "Unknown error";
//            }
//        } catch (Exception parseException) {
//            log.warn("Failed to parse error message from response: {}", ex.contentUTF8(), parseException);
//        }
//        return "An unknown error occurred.";
//    }
//    /**
//     * Ensures a valid token by delegating to the token refresh handler.
//     *
//     * @return a valid authentication token
//     */
//    private String getValidToken() {
//        tokenHandler.ensureValidToken();
//        String token = tokenHandler.getToken();
//        if (token == null) {
//            throw new PaymentGatewayException("Failed to retrieve a valid token.");
//        }
//        return token;
//    }
//
//    /**
//     * Builds the Authorization header using the provided token.
//     *
//     * @param token authentication token
//     * @return the Authorization header string
//     */
//    private String buildAuthorizationHeader(String token) {
//        return "Bearer " + token;
//    }
//
//    /**
//     * Maps the PaymentSessionRequest to the API's expected SessionRequest format.
//     * If the MerchantTransactionId is missing, auto-generate it based on:
//     * - The first letter of the merchant name.
//     * - Current date in yyyyMMdd format.
//     * - Sequential number starting from 01.
//     *
//     * @param request the payment session request DTO
//     * @return the mapped SessionRequest object
//     */
//    private SessionRequest mapToSessionRequest(PaymentSessionRequest request) {
//        String merchantTransactionId = request.getMerchantTransactionId();
//        if (merchantTransactionId == null || merchantTransactionId.trim().isEmpty()) {
//            merchantTransactionId = generateMerchantTransactionId(request.getMerchantName());
//            log.info("Generated MerchantTransactionId: {}", merchantTransactionId);
//
//            // Update the original request with the generated ID
//            request.setMerchantTransactionId(merchantTransactionId);
//        }
//
//        return SessionRequest.builder()
//                .amount(SessionRequest.Amount.builder()
//                        .amountInCents(request.getAmountInCents())
//                        .currency(request.getCurrency())
//                        .build())
//                .merchant(SessionRequest.Merchant.builder()
//                        .name(request.getMerchantName())
//                        .build())
//                .paymentType(PAYMENT_TYPE_PURCHASE)
//                .merchantTransactionId(merchantTransactionId)
//                .description(request.getDescription())
//                .returnUrl(request.getReturnUrl())
//                .build();
//    }
//
//    /**
//     * Generates a unique MerchantTransactionId based on the following:
//     * - The first letter of the merchant name.
//     * - Current date in yyyyMMdd format.
//     * - Sequential number starting from 01.
//     *
//     * @param merchantName the merchant name
//     * @return the generated MerchantTransactionId
//     */
//    private String generateMerchantTransactionId(String merchantName) {
//        String firstLetter = merchantName != null && !merchantName.trim().isEmpty()
//                ? merchantName.substring(0, 1).toUpperCase()
//                : "X"; // Default to 'X' if merchant name is empty
//
//        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
//
//        int sequenceNumber = sequentialCounter.incrementAndGet(); // Increment the atomic counter
//        String formattedSequence = String.format("%02d", sequenceNumber); // Format as 01, 02, etc.
//
//        return firstLetter + date + formattedSequence;
//    }
//
//    /**
//     * Parses the API response map into the SessionResponse object.
//     *
//     * @param responseMap the raw response map
//     * @return the parsed SessionResponse object
//     */
//    private SessionResponse parseGatewayResponse(Map<String, Object> responseMap) {
//        SessionResponse gatewayResponse = objectMapper.convertValue(responseMap, SessionResponse.class);
//        if (gatewayResponse.getError() != null) {
//            log.error("Error from payment gateway: {}", gatewayResponse.getError().getMessage());
//            throw new PaymentGatewayException("Payment gateway error: " + gatewayResponse.getError().getMessage());
//        }
//        return gatewayResponse;
//    }
//
//    /**
//     * Extracts the session ID from the API response.
//     *
//     * @param gatewayResponse the parsed SessionResponse object
//     * @return the session ID
//     */
//    private String extractSessionId(SessionResponse gatewayResponse) {
//        if (gatewayResponse.getData() == null || gatewayResponse.getData().getId() == null) {
//            throw new PaymentGatewayException("Session ID is missing in the API response.");
//        }
//        return gatewayResponse.getData().getId();
//    }
//
//    /**
//     * Constructs the payment page URL using the session ID.
//     *
//     * @param sessionId the session ID
//     * @return the payment page URL
//     */
//    private String constructPaymentPageUrl(String sessionId) {
//        return paymentPageBaseUrl + "/" + sessionId;
//    }
//
//    /**
//     * Builds the PaymentSessionResponse DTO to return to the client.
//     *
//     * @param sessionId the session ID
//     * @param paymentUrl the payment page URL
//     * @param status the session status
//     * @param request the original PaymentSessionRequest
//     * @return the PaymentSessionResponse DTO
//     */
//    private PaymentSessionResponse buildPaymentSessionResponse(String sessionId, String paymentUrl, String status, PaymentSessionRequest request) {
//        return PaymentSessionResponse.builder()
//                .sessionId(sessionId)
//                .paymentUrl(paymentUrl)
//                .status(status)
//                .merchantTransactionId(request.getMerchantTransactionId()) // Updated to include the generated ID
//                .amount(PaymentSessionResponse.PaymentAmount.builder()
//                        .amountInCents(request.getAmountInCents())
//                        .currency(request.getCurrency())
//                        .build())
//                .build();
//    }
//}








//package co.zw.telone.paymentgateway.service.impl;
//
//import co.zw.telone.paymentgateway.client.SessionApiClient;
//import co.zw.telone.paymentgateway.config.InterceptorTokenRefreshHandler;
//import co.zw.telone.paymentgateway.exception.PaymentGatewayException;
//import co.zw.telone.paymentgateway.response.PaymentSessionRequest;
//import co.zw.telone.paymentgateway.response.PaymentSessionResponse;
//import co.zw.telone.paymentgateway.response.SessionRequest;
//import co.zw.telone.paymentgateway.response.SessionResponse;
//import co.zw.telone.paymentgateway.service.PaymentGatewayService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static co.zw.telone.paymentgateway.util.PaymentGatewayConstants.PAYMENT_SESSION_FAILED;
//import static co.zw.telone.paymentgateway.util.PaymentGatewayConstants.PAYMENT_TYPE_PURCHASE;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentGatewayServiceImpl implements PaymentGatewayService {
//
//    private final SessionApiClient sessionApiClient;
//    private final InterceptorTokenRefreshHandler tokenHandler;
//    private final ObjectMapper objectMapper;
//
//    @Value("${payment.gateway.payment-page-base-url}")
//    private String paymentPageBaseUrl;
//
//    // Atomic counter for sequential numbering
//    private final AtomicInteger sequentialCounter = new AtomicInteger(0);
//
//    @Override
//    public PaymentSessionResponse createPaymentSession(PaymentSessionRequest request) {
//        log.info("Creating payment session for merchantTransactionId: {}", request.getMerchantTransactionId());
//
//        try {
//            // Ensure a valid token
//            String token = getValidToken();
//            String authHeader = buildAuthorizationHeader(token);
//
//            // Map the incoming request to the session request format expected by the API
//            SessionRequest sessionRequest = mapToSessionRequest(request);
//
//            // Call the session API
//            Map<String, Object> responseMap = sessionApiClient.createSession(authHeader, sessionRequest);
//            log.debug("Session API response: {}", responseMap);
//
//            // Parse the API response
//            SessionResponse gatewayResponse = parseGatewayResponse(responseMap);
//
//            // Construct the payment page URL using the session ID
//            String sessionId = extractSessionId(gatewayResponse);
//            String paymentPageUrl = constructPaymentPageUrl(sessionId);
//
//            // Build and return the response DTO
//            return buildPaymentSessionResponse(sessionId, paymentPageUrl, gatewayResponse.getData().getSessionInfo().getStatus(), request);
//
//        } catch (Exception e) {
//            log.error("Error creating payment session for merchantTransactionId: {}", request.getMerchantTransactionId(), e);
//            throw new PaymentGatewayException(PAYMENT_SESSION_FAILED, e);
//        }
//    }
//
//    /**
//     * Ensures a valid token by delegating to the token refresh handler.
//     *
//     * @return a valid authentication token
//     */
//    private String getValidToken() {
//        tokenHandler.ensureValidToken();
//        String token = tokenHandler.getToken();
//        if (token == null) {
//            throw new PaymentGatewayException("Failed to retrieve a valid token.");
//        }
//        return token;
//    }
//
//    /**
//     * Builds the Authorization header using the provided token.
//     *
//     * @param token authentication token
//     * @return the Authorization header string
//     */
//    private String buildAuthorizationHeader(String token) {
//        return "Bearer " + token;
//    }
//
//    /**
//     * Maps the PaymentSessionRequest to the API's expected SessionRequest format.
//     * If the MerchantTransactionId is missing, auto-generate it based on:
//     * - The first letter of the merchant name.
//     * - Current date in yyyyMMdd format.
//     * - Sequential number starting from 01.
//     *
//     * @param request the payment session request DTO
//     * @return the mapped SessionRequest object
//     */
//    private SessionRequest mapToSessionRequest(PaymentSessionRequest request) {
//        String merchantTransactionId = request.getMerchantTransactionId();
//        if (merchantTransactionId == null || merchantTransactionId.trim().isEmpty()) {
//            merchantTransactionId = generateMerchantTransactionId(request.getMerchantName());
//            log.info("Generated MerchantTransactionId: {}", merchantTransactionId);
//        }
//
//        return SessionRequest.builder()
//                .amount(SessionRequest.Amount.builder()
//                        .amountInCents(request.getAmountInCents())
//                        .currency(request.getCurrency())
//                        .build())
//                .merchant(SessionRequest.Merchant.builder()
//                        .name(request.getMerchantName())
//                        .build())
//                .paymentType(PAYMENT_TYPE_PURCHASE)
//                .merchantTransactionId(merchantTransactionId)
//                .description(request.getDescription())
//                .returnUrl(request.getReturnUrl())
//                .build();
//    }
//
//    /**
//     * Generates a unique MerchantTransactionId based on the following:
//     * - The first letter of the merchant name.
//     * - Current date in yyyyMMdd format.
//     * - Sequential number starting from 01.
//     *
//     * @param merchantName the merchant name
//     * @return the generated MerchantTransactionId
//     */
//    private String generateMerchantTransactionId(String merchantName) {
//        String firstLetter = merchantName != null && !merchantName.trim().isEmpty()
//                ? merchantName.substring(0, 1).toUpperCase()
//                : "X"; // Default to 'X' if merchant name is empty
//
//        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
//
//        int sequenceNumber = sequentialCounter.incrementAndGet(); // Increment the atomic counter
//        String formattedSequence = String.format("%02d", sequenceNumber); // Format as 01, 02, etc.
//
//        return firstLetter + date + formattedSequence;
//    }
//
//    /**
//     * Parses the API response map into the SessionResponse object.
//     *
//     * @param responseMap the raw response map
//     * @return the parsed SessionResponse object
//     */
//    private SessionResponse parseGatewayResponse(Map<String, Object> responseMap) {
//        SessionResponse gatewayResponse = objectMapper.convertValue(responseMap, SessionResponse.class);
//        if (gatewayResponse.getError() != null) {
//            log.error("Error from payment gateway: {}", gatewayResponse.getError().getMessage());
//            throw new PaymentGatewayException("Payment gateway error: " + gatewayResponse.getError().getMessage());
//        }
//        return gatewayResponse;
//    }
//
//    /**
//     * Extracts the session ID from the API response.
//     *
//     * @param gatewayResponse the parsed SessionResponse object
//     * @return the session ID
//     */
//    private String extractSessionId(SessionResponse gatewayResponse) {
//        if (gatewayResponse.getData() == null || gatewayResponse.getData().getId() == null) {
//            throw new PaymentGatewayException("Session ID is missing in the API response.");
//        }
//        return gatewayResponse.getData().getId();
//    }
//
//    /**
//     * Constructs the payment page URL using the session ID.
//     *
//     * @param sessionId the session ID
//     * @return the payment page URL
//     */
//    private String constructPaymentPageUrl(String sessionId) {
//        return paymentPageBaseUrl + "/" + sessionId;
//    }
//
//    /**
//     * Builds the PaymentSessionResponse DTO to return to the client.
//     *
//     * @param sessionId the session ID
//     * @param paymentUrl the payment page URL
//     * @param status the session status
//     * @param request the original PaymentSessionRequest
//     * @return the PaymentSessionResponse DTO
//     */
//    private PaymentSessionResponse buildPaymentSessionResponse(String sessionId, String paymentUrl, String status, PaymentSessionRequest request) {
//        return PaymentSessionResponse.builder()
//                .sessionId(sessionId)
//                .paymentUrl(paymentUrl)
//                .status(status)
//                .merchantTransactionId(request.getMerchantTransactionId())
//                .amount(PaymentSessionResponse.PaymentAmount.builder()
//                        .amountInCents(request.getAmountInCents())
//                        .currency(request.getCurrency())
//                        .build())
//                .build();
//    }
//}










//package co.zw.telone.paymentgateway.service.impl;
//
//import co.zw.telone.paymentgateway.client.SessionApiClient;
//import co.zw.telone.paymentgateway.config.InterceptorTokenRefreshHandler;
//import co.zw.telone.paymentgateway.exception.PaymentGatewayException;
//import co.zw.telone.paymentgateway.response.PaymentSessionRequest;
//import co.zw.telone.paymentgateway.response.PaymentSessionResponse;
//import co.zw.telone.paymentgateway.response.SessionRequest;
//import co.zw.telone.paymentgateway.response.SessionResponse;
//import co.zw.telone.paymentgateway.service.PaymentGatewayService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//import static co.zw.telone.paymentgateway.util.PaymentGatewayConstants.PAYMENT_SESSION_FAILED;
//import static co.zw.telone.paymentgateway.util.PaymentGatewayConstants.PAYMENT_TYPE_PURCHASE;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentGatewayServiceImpl implements PaymentGatewayService {
//
//    private final SessionApiClient sessionApiClient;
//    private final InterceptorTokenRefreshHandler tokenHandler;
//    private final ObjectMapper objectMapper;
//
//    @Value("${payment.gateway.payment-page-base-url}")
//    private String paymentPageBaseUrl;
//
//    @Override
//    public PaymentSessionResponse createPaymentSession(PaymentSessionRequest request) {
//        log.info("Creating payment session for merchantTransactionId: {}", request.getMerchantTransactionId());
//
//        try {
//            // Ensure a valid token
//            String token = getValidToken();
//            String authHeader = buildAuthorizationHeader(token);
//
//            // Map the incoming request to the session request format expected by the API
//            SessionRequest sessionRequest = mapToSessionRequest(request);
//
//            // Call the session API
//            Map<String, Object> responseMap = sessionApiClient.createSession(authHeader, sessionRequest);
//            log.debug("Session API response: {}", responseMap);
//
//            // Parse the API response
//            SessionResponse gatewayResponse = parseGatewayResponse(responseMap);
//
//            // Construct the payment page URL using the session ID
//            String sessionId = extractSessionId(gatewayResponse);
//            String paymentPageUrl = constructPaymentPageUrl(sessionId);
//
//            // Build and return the response DTO
//            return buildPaymentSessionResponse(sessionId, paymentPageUrl, gatewayResponse.getData().getSessionInfo().getStatus(), request);
//
//        } catch (Exception e) {
//            log.error("Error creating payment session for merchantTransactionId: {}", request.getMerchantTransactionId(), e);
//            throw new PaymentGatewayException(PAYMENT_SESSION_FAILED, e);
//        }
//    }
//
//    /**
//     * Ensures a valid token by delegating to the token refresh handler.
//     *
//     * @return a valid authentication token
//     */
//    private String getValidToken() {
//        tokenHandler.ensureValidToken();
//        String token = tokenHandler.getToken();
//        if (token == null) {
//            throw new PaymentGatewayException("Failed to retrieve a valid token.");
//        }
//        return token;
//    }
//
//    /**
//     * Builds the Authorization header using the provided token.
//     *
//     * @param token authentication token
//     * @return the Authorization header string
//     */
//    private String buildAuthorizationHeader(String token) {
//        return "Bearer " + token;
//    }
//
//    /**
//     * Maps the PaymentSessionRequest to the API's expected SessionRequest format.
//     *
//     * @param request the payment session request DTO
//     * @return the mapped SessionRequest object
//     */
//    private SessionRequest mapToSessionRequest(PaymentSessionRequest request) {
//        return SessionRequest.builder()
//                .amount(SessionRequest.Amount.builder()
//                        .amountInCents(request.getAmountInCents())
//                        .currency(request.getCurrency())
//                        .build())
//                .merchant(SessionRequest.Merchant.builder()
//                        .name(request.getMerchantName())
//                        .build())
//                .paymentType(PAYMENT_TYPE_PURCHASE)
//                .merchantTransactionId(request.getMerchantTransactionId())
//                .description(request.getDescription())
//                .returnUrl(request.getReturnUrl())
//                .build();
//    }
//
//    /**
//     * Parses the API response map into the SessionResponse object.
//     *
//     * @param responseMap the raw response map
//     * @return the parsed SessionResponse object
//     */
//    private SessionResponse parseGatewayResponse(Map<String, Object> responseMap) {
//        SessionResponse gatewayResponse = objectMapper.convertValue(responseMap, SessionResponse.class);
//        if (gatewayResponse.getError() != null) {
//            log.error("Error from payment gateway: {}", gatewayResponse.getError().getMessage());
//            throw new PaymentGatewayException("Payment gateway error: " + gatewayResponse.getError().getMessage());
//        }
//        return gatewayResponse;
//    }
//
//    /**
//     * Extracts the session ID from the API response.
//     *
//     * @param gatewayResponse the parsed SessionResponse object
//     * @return the session ID
//     */
//    private String extractSessionId(SessionResponse gatewayResponse) {
//        if (gatewayResponse.getData() == null || gatewayResponse.getData().getId() == null) {
//            throw new PaymentGatewayException("Session ID is missing in the API response.");
//        }
//        return gatewayResponse.getData().getId();
//    }
//
//    /**
//     * Constructs the payment page URL using the session ID.
//     *
//     * @param sessionId the session ID
//     * @return the payment page URL
//     */
//    private String constructPaymentPageUrl(String sessionId) {
//        return paymentPageBaseUrl + "/" + sessionId;
//    }
//
//    /**
//     * Builds the PaymentSessionResponse DTO to return to the client.
//     *
//     * @param sessionId the session ID
//     * @param paymentUrl the payment page URL
//     * @param status the session status
//     * @param request the original PaymentSessionRequest
//     * @return the PaymentSessionResponse DTO
//     */
//    private PaymentSessionResponse buildPaymentSessionResponse(String sessionId, String paymentUrl, String status, PaymentSessionRequest request) {
//        return PaymentSessionResponse.builder()
//                .sessionId(sessionId)
//                .paymentUrl(paymentUrl)
//                .status(status)
//                .merchantTransactionId(request.getMerchantTransactionId())
//                .amount(PaymentSessionResponse.PaymentAmount.builder()
//                        .amountInCents(request.getAmountInCents())
//                        .currency(request.getCurrency())
//                        .build())
//                .build();
//    }
//}