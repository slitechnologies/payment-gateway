//package co.zw.telone.paymentgateway.controller;
//
//import co.zw.telone.paymentgateway.paymentDto.*;
//import co.zw.telone.paymentgateway.service.PaymentGatewayService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.view.RedirectView;
//
//import java.net.URI;
//
//@Controller
//@RequestMapping("/api/payments")
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentGatewayController {
//
//    private final PaymentGatewayService paymentGatewayService;
//
//    /**
//     * API endpoint that returns the payment session details in a JSON response
//     */
//    @PostMapping("/create-session")
//    @ResponseBody
//    public ResponseEntity<PaymentSessionResponse> createPaymentSession(@RequestBody PaymentSessionRequest request) {
//        PaymentSessionResponse response = paymentGatewayService.createPaymentSession(request);
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Endpoint that directly redirects the user to the payment page
//     */
//    @PostMapping("/redirect-to-payment")
//    public ResponseEntity<Void> redirectToPayment(@RequestBody PaymentSessionRequest_ request) {
//        PaymentSessionResponse_ response = paymentGatewayService.createPaymentSession(request);
//
//        // Create HTTP redirect response
//        HttpHeaders headers = new HttpHeaders();
//        headers.setLocation(URI.create(response.getPaymentUrl()));
//
//        return new ResponseEntity<>(headers, HttpStatus.FOUND);
//    }
//
//    /**
//     * Alternative redirect implementation using Spring's RedirectView
//     */
//    @PostMapping("/redirect-to-payment-alt")
//    public RedirectView redirectToPaymentAlt(@RequestBody PaymentSessionRequest_ request) {
//        PaymentSessionResponse_ response = paymentGatewayService.createPaymentSession(request);
//        return new RedirectView(response.getPaymentUrl());
//    }
//
//    /**
//     * Handles the return callback from the payment gateway
//     */
//    @GetMapping("/payment-callback")
//    @ResponseBody
//    public ResponseEntity<PaymentCallbackResponse> handlePaymentCallback(
//            @RequestParam("status") String status,
//            @RequestParam("statusCode") String statusCode,
//            @RequestParam("merchantTransactionId") String merchantTransactionId) {
//
//        log.info("Payment callback received: status={}, statusCode={}, merchantTransactionId={}",
//                status, statusCode, merchantTransactionId);
//
//        boolean isSuccessful = "Complete".equals(status) && "200".equals(statusCode);
//
//        // Here you would typically update your database with the payment result
//        // paymentService.updatePaymentStatus(merchantTransactionId, isSuccessful);
//
//        PaymentCallbackResponse response = new PaymentCallbackResponse();
//        response.setSuccess(isSuccessful);
//        response.setMessage(isSuccessful ? "Payment completed successfully" : "Payment failed");
//        response.setTransactionId(merchantTransactionId);
//
//        return ResponseEntity.ok(response);
//    }
//}
//
//
//
//
//
//
//
////package co.zw.telone.paymentgateway.controller;
////
////import co.zw.telone.paymentgateway.paymentDto.PaymentSessionRequest;
////import co.zw.telone.paymentgateway.paymentDto.PaymentCallbackResponse;
////import co.zw.telone.paymentgateway.service.PaymentGatewayService;
////import lombok.RequiredArgsConstructor;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.http.HttpHeaders;
////import org.springframework.http.HttpStatus;
////import org.springframework.http.ResponseEntity;
////import org.springframework.stereotype.Controller;
////import org.springframework.web.bind.annotation.*;
////import org.springframework.web.servlet.view.RedirectView;
////
////import java.net.URI;
////import java.util.HashMap;
////import java.util.Map;
////
////@Controller
////@RequestMapping("/api/payments")
////@RequiredArgsConstructor
////@Slf4j
////public class PaymentGatewayController {
////
////    private final PaymentGatewayService paymentGatewayService;
////
////    /**
////     * API endpoint that returns the payment URL in a JSON response
////     */
////    @PostMapping("/create-session")
////    @ResponseBody
////    public ResponseEntity<Map<String, String>> createPaymentSession(@RequestBody PaymentSessionRequest request) {
////        String paymentPageUrl = paymentGatewayService.createPaymentSession(
////                request.getAmountInCents(),
////                request.getCurrency(),
////                request.getMerchantName(),
////                request.getMerchantTransactionId(),
////                request.getDescription(),
////                request.getReturnUrl()
////        );
////
////        Map<String, String> response = new HashMap<>();
////        response.put("paymentUrl", paymentPageUrl);
////
////        return ResponseEntity.ok(response);
////    }
////
////    /**
////     * Endpoint that directly redirects the user to the payment page
////     */
////    @PostMapping("/redirect-to-payment")
////    public ResponseEntity<Void> redirectToPayment(@RequestBody PaymentSessionRequest request) {
////        String paymentPageUrl = paymentGatewayService.createPaymentSession(
////                request.getAmountInCents(),
////                request.getCurrency(),
////                request.getMerchantName(),
////                request.getMerchantTransactionId(),
////                request.getDescription(),
////                request.getReturnUrl()
////        );
////
////        // Create HTTP redirect response
////        HttpHeaders headers = new HttpHeaders();
////        headers.setLocation(URI.create(paymentPageUrl));
////
////        return new ResponseEntity<>(headers, HttpStatus.FOUND);
////    }
////
////    /**
////     * Alternative redirect implementation using Spring's RedirectView
////     */
////    @PostMapping("/redirect-to-payment-alt")
////    public RedirectView redirectToPaymentAlt(@RequestBody PaymentSessionRequest request) {
////        String paymentPageUrl = paymentGatewayService.createPaymentSession(
////                request.getAmountInCents(),
////                request.getCurrency(),
////                request.getMerchantName(),
////                request.getMerchantTransactionId(),
////                request.getDescription(),
////                request.getReturnUrl()
////        );
////
////        return new RedirectView(paymentPageUrl);
////    }
////
////    /**
////     * Handles the return callback from the payment gateway
////     */
////    @GetMapping("/payment-callback")
////    @ResponseBody
////    public ResponseEntity<PaymentCallbackResponse> handlePaymentCallback(
////            @RequestParam("status") String status,
////            @RequestParam("statusCode") String statusCode,
////            @RequestParam("merchantTransactionId") String merchantTransactionId) {
////
////        log.info("Payment callback received: status={}, statusCode={}, merchantTransactionId={}",
////                status, statusCode, merchantTransactionId);
////
////        boolean isSuccessful = "Complete".equals(status) && "200".equals(statusCode);
////
////        // Here you would typically update your database with the payment result
////        // paymentGatewayService.updatePaymentStatus(merchantTransactionId, isSuccessful);
////
////        PaymentCallbackResponse response = new PaymentCallbackResponse();
////        response.setSuccess(isSuccessful);
////        response.setMessage(isSuccessful ? "Payment completed successfully" : "Payment failed");
////        response.setTransactionId(merchantTransactionId);
////
////        return ResponseEntity.ok(response);
////    }
////}
//
//
//
////package co.zw.telone.paymentgateway.controller;
////
////
////import co.zw.telone.paymentgateway.paymentDto.PaymentSessionRequest;
////import co.zw.telone.paymentgateway.service.PaymentGatewayService;
////import lombok.RequiredArgsConstructor;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.PostMapping;
////import org.springframework.web.bind.annotation.RequestBody;
////import org.springframework.web.bind.annotation.RequestMapping;
////import org.springframework.web.bind.annotation.RestController;
////
////import java.util.HashMap;
////import java.util.Map;
////
////@RestController
////@RequestMapping("/api/payments")
////@RequiredArgsConstructor
////public class PaymentGatewayController {
////
////    private final PaymentGatewayService paymentGatewayService;
////
////    @PostMapping("/create-session")
////    public ResponseEntity<Map<String, String>> createPaymentSession(@RequestBody PaymentSessionRequest request) {
////        String paymentPageUrl = paymentGatewayService.createPaymentSession(
////                request.getAmountInCents(),
////                request.getCurrency(),
////                request.getMerchantName(),
////                request.getMerchantTransactionId(),
////                request.getDescription(),
////                request.getReturnUrl()
////        );
////
////        Map<String, String> response = new HashMap<>();
////        response.put("paymentUrl", paymentPageUrl);
////
////        return ResponseEntity.ok(response);
////    }
////}
