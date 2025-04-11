package co.zw.telone.paymentgateway.controller;


import co.zw.telone.paymentgateway.tokendto.ApiResponse;
import co.zw.telone.paymentgateway.tokendto.TokenResponse;
import co.zw.telone.paymentgateway.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @GetMapping
    public ResponseEntity<ApiResponse<TokenResponse>> getToken() {
        TokenResponse tokenResponse = tokenService.getAuthenticationToken();
        return ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }
}





