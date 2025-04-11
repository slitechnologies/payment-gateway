package co.zw.telone.paymentgateway;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "Payment Gateway Service",
        description = "This Service is Responsible For Processing All The Digital Innovation Payments", version = "0.0.1"))
@SpringBootApplication
@EnableFeignClients(basePackages = "co.zw.telone.paymentgateway.client")
public class PaymentGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentGatewayApplication.class, args);
    }

}
