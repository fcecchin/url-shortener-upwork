package com.upwork.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record ShortUrlRequest(@JsonProperty @NotNull(message = "Missing field: url") String url,
                              @JsonProperty("valid-days") @NotNull(message = "Missing field: valid-days") Integer validDays) {
}
