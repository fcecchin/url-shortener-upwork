package com.upwork.urlshortener.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "hashed_key", unique = true, nullable = false)
    @Length(max = 8)
    private String key;

    @Column
    @NotNull
    private String originalUrl;

    @Column
    @NotNull
    private String url;

    @Column
    @NotNull
    private LocalDateTime expiresAt;

    @Column
    @NotNull
    @Builder.Default
    private Long redirects = 0L;
}

