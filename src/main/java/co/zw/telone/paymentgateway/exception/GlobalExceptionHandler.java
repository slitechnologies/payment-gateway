package co.zw.telone.paymentgateway.exception;

import co.zw.telone.paymentgateway.strategy.context.ErrorHandlerContext;
import co.zw.telone.paymentgateway.strategy.impl.FeignBadRequestStrategy;
import co.zw.telone.paymentgateway.tokendto.ApiResponse;
import co.zw.telone.paymentgateway.tokendto.ErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

}