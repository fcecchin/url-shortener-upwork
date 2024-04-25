package com.upwork.urlshortener.service;

import com.upwork.urlshortener.dto.ShortUrlRequest;
import com.upwork.urlshortener.dto.ShortUrlResponse;
import com.upwork.urlshortener.exception.InvalidUrlException;
import com.upwork.urlshortener.exception.ResourceNotFoundException;
import com.upwork.urlshortener.hash.Hash;
import com.upwork.urlshortener.model.ShortUrl;
import com.upwork.urlshortener.repository.ShortUrlRepository;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;

@Service
public class ShortUrlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShortUrlService.class.getName());
    private final ShortUrlRepository repository;
    private final Hash hashAlgorithm;

    @Value("${application.valid-days}")
    private int validDays;

    public ShortUrlService(ShortUrlRepository repository, Hash hashAlgo) {
        this.repository = repository;
        this.hashAlgorithm = hashAlgo;
    }

    public ShortUrlResponse create(ShortUrlRequest requestBody, String context) {
        validateUrl(requestBody.url());
        String hashed = hashAlgorithm.hash(requestBody.url());
        ShortUrl saved = repository.findByKey(hashed).orElseGet(() -> {
            ShortUrl url = ShortUrl.builder()
                    .originalUrl(requestBody.url())
                    .key(hashed)
                    .url(context + "/" + hashed)
                    .expiresAt(LocalDateTime.now().plusDays(validDays))
                    .build();
            return repository.save(url);
        });
        LOGGER.info("URL created from {} to {}", saved.getOriginalUrl(), saved.getUrl());
        return ShortUrlResponse.from(saved);
    }

    @Cacheable(value = "hashedUrls", key = "#hashed", unless = "#result.redirects < 10")
    public ShortUrl redirect(String hashed) {
        ShortUrl url = findByHash(hashed);
        // increment number of redirects (clicks) done to this URL
        url.setRedirects(url.getRedirects() + 1);
        repository.save(url);
        return url;
    }


    public ShortUrl findByHash(String hashed) {
        return repository.findByKey(hashed)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));
    }


    public void delete(String hashed) {
        ShortUrl url = findByHash(hashed);

        repository.delete(url);
        LOGGER.info("URL deleted: {}", hashed);
    }

    /**
     * Scheduled task that purgues all expired URLs. Runs every day at 1am
     */
    @Scheduled(cron = "0 0 1 * * *")
    @CacheEvict(value = "hashedUrls", allEntries = true)
    public void purgeExpiredUrls() {
        repository.deleteAllExpired(LocalDateTime.now());
        LOGGER.info("Expired URLs were purged");
    }

    private void validateUrl(@NotNull(message = "Missing field: url") String url) {
        try {
            URI.create(url).toURL();
        } catch (Exception e) {
            LOGGER.error("Invalid URL: {}", url);
            throw new InvalidUrlException("Invalid URL: " + e.getMessage());
        }
    }
}
