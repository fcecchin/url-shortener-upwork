package com.upwork.urlshortener.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "short_url")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "hashed_key", unique = true)
    private String key;

    @Column
    private String originalUrl;

    @Column
    private String url;

    @Column
    private LocalDateTime expiresAt;

    @Column
    @Builder.Default
    private Long redirects = 0L;
}

