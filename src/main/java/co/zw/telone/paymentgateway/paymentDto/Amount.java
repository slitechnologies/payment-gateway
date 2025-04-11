package co.zw.telone.paymentgateway.paymentDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Amount {
    private Long amountInCents;
    private String currency;
}
