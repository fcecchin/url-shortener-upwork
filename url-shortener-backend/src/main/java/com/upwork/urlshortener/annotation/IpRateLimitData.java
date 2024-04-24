package com.upwork.urlshortener.annotation;

import lombok.*;

import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.SECONDS;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IpRateLimitData {
    private int amount;
    private LocalDateTime requestDate;

    public static IpRateLimitData setDefaultData() {
        return new IpRateLimitData(1, LocalDateTime.now());
    }

    public long getTimeDiffInSeconds() {
        return requestDate.until(LocalDateTime.now(), SECONDS);
    }
}