package co.zw.telone.paymentgateway.strategy.context;

import co.zw.telone.paymentgateway.strategy.ErrorStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RequiredArgsConstructor
public class ErrorHandlerContext {

    private final ErrorStrategy errorStrategy;

    public ResponseEntity<Map<String, Object>> handleError(String responseBody) {
        return errorStrategy.handleError(responseBody);
    }
}