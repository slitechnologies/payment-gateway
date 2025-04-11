package co.zw.telone.paymentgateway.service.impl;



import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RedisUniqueIdService {


    private final StringRedisTemplate redisTemplate;

    public String generateUniqueTransactionId(String merchantName) {
        String firstLetter = merchantName != null && !merchantName.trim().isEmpty()
                ? merchantName.substring(0, 1).toUpperCase()
                : "X";

        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());

        for (int i = 1; ; i++) {
            String transactionId = firstLetter + date + String.format("%02d", i);

            // Attempt to store the ID in Redis with a TTL of 1 day
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(transactionId, "LOCK", 1, TimeUnit.DAYS);

            if (Boolean.TRUE.equals(isNew)) {
                // ID is unique, return it
                return transactionId;
            }
        }
    }
}