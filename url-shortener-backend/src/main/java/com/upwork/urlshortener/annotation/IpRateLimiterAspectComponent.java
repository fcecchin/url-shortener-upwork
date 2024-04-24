package com.upwork.urlshortener.annotation;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class IpRateLimiterAspectComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpRateLimiterAspectComponent.class);
    private final Map<String, IpRateLimitData> ipRateLimitDataMap = new HashMap<>();

    @Around("@annotation(ipRateLimit)")
    public Object limit(ProceedingJoinPoint joinPoint, IpRateLimit ipRateLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getRemoteAddr();
        LOGGER.info("IP '{}' request", ip);
        String key = "hash-" + Base64.getEncoder().encodeToString(ip.getBytes());

        if (isRateLimited(key, ipRateLimit.limit(), ipRateLimit.duration())) {
            LOGGER.info("IP '{}' has request limit", ip);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded for IP: " + ip);
        }
        LOGGER.info("IP '{}' has not request limit", ip);
        return joinPoint.proceed();
    }

    public boolean isRateLimited(String key, int limit, int duration) {
        IpRateLimitData ipRateLimitData = ipRateLimitDataMap.get(key);

        boolean finalAns = false;

        if (ipRateLimitData == null) {
            ipRateLimitData = IpRateLimitData.setDefaultData();
        } else {
            if (ipRateLimitData.getAmount() >= limit) {
                if (ipRateLimitData.getTimeDiffInSeconds() < duration) {
                    finalAns = true;
                } else {
                    ipRateLimitData = IpRateLimitData.setDefaultData();
                }
            } else {
                ipRateLimitData.setAmount(ipRateLimitData.getAmount() + 1);
            }
        }

        LOGGER.info("Key '{}' sent reuqests '{}' times", key, ipRateLimitData.getAmount());

        ipRateLimitDataMap.put(key, ipRateLimitData);

        return finalAns;
    }
}