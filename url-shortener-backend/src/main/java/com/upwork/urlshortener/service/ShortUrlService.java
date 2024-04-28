package com.upwork.urlshortener.service;

import com.upwork.urlshortener.dto.ShortUrlRequest;
import com.upwork.urlshortener.dto.ShortUrlResponse;
import com.upwork.urlshortener.exception.InvalidCustomKeyException;
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
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

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
        String decodedUrl = URLDecoder.decode(requestBody.url(), StandardCharsets.UTF_8);
        validateUrl(decodedUrl);
        boolean isCustomKey = StringUtils.hasText(requestBody.customKey());
        String key = isCustomKey ? requestBody.customKey() : hashAlgorithm.hash(decodedUrl);
        Optional<ShortUrl> opUrl = repository.findByKey(key);
        if (opUrl.isPresent() && isCustomKey) {
            LOGGER.info("URL already created from {} to {}", opUrl.get().getOriginalUrl(), opUrl.get().getUrl());
            throw new InvalidCustomKeyException("Custom URL already in use");
        }
        ShortUrl saved = opUrl.orElseGet(() -> {
            ShortUrl url = ShortUrl.builder()
                    .originalUrl(requestBody.url())
                    .key(key)
                    .url(context + "/" + key)
                    .createdAt(LocalDateTime.now())
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
        repository.findAndIncrementVisitsByKey(url.getKey(), LocalDateTime.now());
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
            //noinspection ResultOfMethodCallIgnored
            URI.create(url).toURL();
        } catch (Exception e) {
            LOGGER.error("Invalid URL: {}", url);
            throw new InvalidUrlException("Invalid URL: " + e.getMessage());
        }
    }
}
