package co.zw.telone.paymentgateway.controller;


import co.zw.telone.paymentgateway.response.PaymentSessionRequest;
import co.zw.telone.paymentgateway.response.PaymentSessionResponse;
import co.zw.telone.paymentgateway.response.ApiResponse;
import co.zw.telone.paymentgateway.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static co.zw.telone.paymentgateway.util.PaymentGatewayConstants.OPERATION_SUCCESS;

@RestController
@RequestMapping("/api/v1/payment")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PaymentGateController {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/session")
    public ResponseEntity<ApiResponse<PaymentSessionResponse>> createPaymentSession(
            @RequestBody PaymentSessionRequest request) {

        PaymentSessionResponse data = paymentGatewayService.createPaymentSession(request);
        ApiResponse<PaymentSessionResponse> response = new ApiResponse<>(
                HttpStatus.OK, OPERATION_SUCCESS, data);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}