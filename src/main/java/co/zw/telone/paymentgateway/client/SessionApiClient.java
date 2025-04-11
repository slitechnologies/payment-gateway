package co.zw.telone.paymentgateway.client;

import co.zw.telone.paymentgateway.response.SessionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "sessionApiClient", url = "${payment.gateway.base-url}")
public interface SessionApiClient {

    @PostMapping("/api/v1/sessions")
    Map<String, Object> createSession(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody SessionRequest sessionRequest);
}









//package co.zw.telone.paymentgateway.client;
//
//import co.zw.telone.paymentgateway.config.PaymentGatewayFeignConfig;
//import co.zw.telone.paymentgateway.paymentDto.SessionRequest;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//import java.util.Map;
//
//@FeignClient(
//        name = "session-api",
//        url = "${payment.gateway.base-url}",
//        configuration = PaymentGatewayFeignConfig.class
//)
//public interface SessionApiClient {
//
//    @PostMapping(
//            path = "/api/v1/sessions",
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    Map<String, Object> createSession(@RequestBody SessionRequest request);
//}
