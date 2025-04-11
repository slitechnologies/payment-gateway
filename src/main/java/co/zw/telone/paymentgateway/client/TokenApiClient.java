package co.zw.telone.paymentgateway.client;

import co.zw.telone.paymentgateway.config.PaymentGatewayFeignConfig;
import co.zw.telone.paymentgateway.tokendto.TokenRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "token-api",
        url = "${payment.gateway.base-url}",
        configuration = PaymentGatewayFeignConfig.class
)
public interface TokenApiClient {

    @PostMapping(
            path = "/api/v1/token",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Map<String, Object> getToken(@RequestBody TokenRequest request);
}









//package co.zw.telone.paymentgateway.client;
//
//import co.zw.telone.paymentgateway.config.PaymentGatewayFeignConfig;
//import co.zw.telone.paymentgateway.dto.TokenRequest;
//import co.zw.telone.paymentgateway.dto.TokenResponse;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@FeignClient(
//        name = "token-api",
//        url = "${payment.gateway.base-url}",
//        configuration = PaymentGatewayFeignConfig.class
//)
//public interface TokenApiClient {
//
//    @PostMapping(
//            path = "/api/v1/token",
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    TokenResponse getToken(@RequestBody TokenRequest request);
//}
