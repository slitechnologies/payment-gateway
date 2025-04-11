package co.zw.telone.paymentgateway.service;

import co.zw.telone.paymentgateway.tokendto.TokenResponse;

public interface TokenService {
    TokenResponse getAuthenticationToken();
}