package co.zw.telone.paymentgateway.strategy.impl;

import co.zw.telone.paymentgateway.strategy.ErrorStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class FeignBadRequestStrategy implements ErrorStrategy {

    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<Map<String, Object>> handleError(String responseBody) {
        try {
            Map<String, Object> parsedResponse = objectMapper.readValue(responseBody, Map.class);

            if (parsedResponse.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) parsedResponse.get("error");
                String status = error.getOrDefault("code", "BAD_REQUEST").toString();
                String message = error.getOrDefault("message", "Unknown error occurred.").toString();
                String errorCode = error.getOrDefault("statusCode", "INVALID_REQUEST").toString();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", Map.of(
                                        "status", status,
                                        "message", message,
                                        "code", errorCode
                                )
                        ));
            } else {
                log.warn("Error object is missing in FeignException response: {}", responseBody);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", Map.of(
                                        "status", "BAD_REQUEST",
                                        "message", "Unknown error occurred.",
                                        "code", "INVALID_REQUEST"
                                )
                        ));
            }
        } catch (Exception e) {
            log.error("Failed to parse FeignException response body: {}", responseBody, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", Map.of(
                                    "status", "BAD_REQUEST",
                                    "message", "Error parsing response body.",
                                    "code", "PARSE_ERROR"
                            )
                    ));
        }
    }
}