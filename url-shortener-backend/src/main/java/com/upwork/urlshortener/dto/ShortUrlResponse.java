package com.upwork.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.upwork.urlshortener.model.ShortUrl;

public record ShortUrlResponse(@JsonProperty String key,
                               @JsonProperty("original_url") String originalUrl,
                               @JsonProperty("short_url") String shortUrl) {

    public static ShortUrlResponse from(ShortUrl model) {
        return new ShortUrlResponse(model.getKey(), model.getOriginalUrl(), model.getUrl());
    }
}
