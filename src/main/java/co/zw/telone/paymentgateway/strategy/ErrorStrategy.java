package co.zw.telone.paymentgateway.strategy;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ErrorStrategy {
    ResponseEntity<Map<String, Object>> handleError(String responseBody);
}