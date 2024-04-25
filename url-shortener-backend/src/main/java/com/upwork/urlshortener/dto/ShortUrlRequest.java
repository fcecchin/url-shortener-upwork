package com.upwork.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ShortUrlRequest(@JsonProperty @NotNull(message = "Missing field: url") String url,
                              @JsonProperty("valid-days") @NotNull(message = "Missing field: valid-days") Integer validDays,
                              @JsonProperty("custom-key") @Size(min = 6, max = 8) String customKey) {
}
