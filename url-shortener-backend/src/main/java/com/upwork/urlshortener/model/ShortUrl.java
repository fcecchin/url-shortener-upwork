package com.upwork.urlshortener.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrl {

    @Id
    private long id;

    private User user;

    @Length(max = 8)
    private String key;

    @NotNull
    private String originalUrl;

    @NotNull
    private String url;

    @NotNull
    private LocalDateTime expiresAt;

    @NotNull
    @Builder.Default
    private Long redirects = 0L;

    @Length(max = 8)
    private String customKey;

    private LocalDateTime createdAt;

    private LocalDateTime visitedAt;
}