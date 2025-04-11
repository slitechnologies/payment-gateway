package co.zw.telone.paymentgateway.service;

import co.zw.telone.paymentgateway.response.PaymentSessionRequest;
import co.zw.telone.paymentgateway.response.PaymentSessionResponse;

public interface PaymentGatewayService {
    PaymentSessionResponse createPaymentSession(PaymentSessionRequest request);
}