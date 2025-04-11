package co.zw.telone.paymentgateway.exception;

import co.zw.telone.paymentgateway.strategy.context.ErrorHandlerContext;
import co.zw.telone.paymentgateway.strategy.impl.FeignBadRequestStrategy;
import co.zw.telone.paymentgateway.tokendto.ApiResponse;
import co.zw.telone.paymentgateway.tokendto.ErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(TokenServiceException.class)
    public ResponseEntity<ApiResponse<?>> handleTokenServiceException(TokenServiceException ex) {
        ErrorDto errorDto = ErrorDto.builder()
                .status(ex.getClass().getSimpleName())
                .message(ex.getMessage())
                .code(ex.getStatus().toString())
                .build();
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(errorDto));
    }

        @ExceptionHandler(FeignException.BadRequest.class)
        public ResponseEntity<Map<String, Object>> handleBadRequest(FeignException.BadRequest ex) {
            // Delegate error handling to the strategy
            ErrorHandlerContext context = new ErrorHandlerContext(new FeignBadRequestStrategy(objectMapper));
            return context.handleError(ex.contentUTF8());
        }


        @ExceptionHandler(org.springframework.data.redis.RedisConnectionFailureException.class)
        public ResponseEntity<Object> handleRedisConnectionFailure(RedisConnectionFailureException ex) {
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("timestamp", LocalDateTime.now());
            errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorDetails.put("error", "Redis Connection Failure");
            errorDetails.put("message", "Unable to connect to Redis server.");
            errorDetails.put("path", "/api/v1/payment/session"); // Path can be dynamically set.

            return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

