package co.zw.telone.paymentgateway.tokendto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields during serialization
public class ApiResponse<T> {

 private String message;
 private Integer code; // HTTP status code (e.g., 200, 400)
 private T data; // The response payload
 private ErrorDto error; // Error details, if any

 // Success response factory method
 public static <T> ApiResponse<T> success(T data) {
  return ApiResponse.<T>builder()
          .message("SUCCESS")
          .code(HttpStatus.OK.value()) // Example: HTTP 200 OK
          .data(data)
          .build();
 }

 public static <T> ApiResponse<T> error(ErrorDto error) {
  return ApiResponse.<T>builder()
          .error(error) // Only set error for failure
          .build();
 }
}