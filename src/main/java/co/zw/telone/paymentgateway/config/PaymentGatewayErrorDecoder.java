package co.zw.telone.paymentgateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.ErrorResponse;

import java.io.InputStream;

@Slf4j
public class PaymentGatewayErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public Exception decode(String methodKey, Response response) {

//
//        try(InputStream bodyIs =  response.body().asInputStream()){
//            ErrorResponse errorResponse = objectMapper.readValue(bodyIs, ErrorResponse.class);
//            return defaultDecoder.decode(methodKey, response);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        // You can add custom error handling here if needed
        // For example, you could parse the error response and create a custom exception

        // For now, we'll use the default decoder
        return defaultDecoder.decode(methodKey, response);
    }
}