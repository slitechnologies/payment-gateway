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

        return defaultDecoder.decode(methodKey, response);
    }
}