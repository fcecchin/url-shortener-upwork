package com.upwork.urlshortener.repository;

import com.upwork.urlshortener.model.ShortUrl;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByKey(String key);

    @Modifying
    @Query("DELETE FROM ShortUrl u WHERE u.expiresAt < :date")
    void deleteAllExpired(LocalDateTime date);
}
