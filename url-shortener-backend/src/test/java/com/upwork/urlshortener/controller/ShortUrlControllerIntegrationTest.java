package com.upwork.urlshortener.controller;

import com.upwork.urlshortener.AbstractSpringIntegrationTest;
import com.upwork.urlshortener.model.ShortUrl;
import com.upwork.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class ShortUrlControllerIntegrationTest extends AbstractSpringIntegrationTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ShortUrlControllerIntegrationTest.class);
    private static final String HASH = "07ecc9";
    private static final String GOOGLE_COM = "https://www.google.com";

    @Autowired
    protected ShortUrlRepository shortUrlRepository;

    public void cleanAllDatabases() {
        shortUrlRepository.deleteAll();
        LOGGER.info("DATABASE CLEANED. LET'S GO!");
    }

    @Test
        void shouldCreateShortUrl() throws Exception {
        String body = """
                {
                  "url": "https://www.google.com",
                  "valid-days": 1
                }
                """;
        this.mockMvc.perform(post("/")
                        .content(body).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value(HASH))
                .andExpect(jsonPath("$.short_url").value("http://localhost/" + HASH))
                .andExpect(jsonPath("$.original_url").value(GOOGLE_COM))
        ;

        Optional<ShortUrl> shortUrl = shortUrlRepository.findByKey(HASH);
        assertTrue(shortUrl.isPresent());
        assertEquals(HASH, shortUrl.get().getKey());
        assertTrue(shortUrl.get().getExpiresAt().isAfter(LocalDateTime.now()));
        assertEquals(GOOGLE_COM, shortUrl.get().getOriginalUrl());
        assertTrue(shortUrl.get().getUrl().endsWith("/" + HASH));
    }

    @Test
    void shouldDeleteShortUrl() throws Exception {
        shortUrlRepository.save(ShortUrl.builder()
                .key(HASH)
                .originalUrl(GOOGLE_COM)
                .url("https://localhost/" + HASH)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build());

        this.mockMvc.perform(delete("/{hashed}", HASH))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRedirectShortUrl() throws Exception {
        shortUrlRepository.save(ShortUrl.builder()
                .key(HASH)
                .originalUrl(GOOGLE_COM)
                .url("https://localhost/" + HASH)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build());

        this.mockMvc.perform(get("/{hashed}", HASH))
                .andDo(print())
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", GOOGLE_COM));
    }

    @Test
    void shouldReturnNotFoundWhenRedirectInvalidShortUrl() throws Exception {
        shortUrlRepository.save(ShortUrl.builder()
                .key(HASH)
                .originalUrl(GOOGLE_COM)
                .url("https://localhost/" + HASH)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build());

        this.mockMvc.perform(get("/{hashed}", HASH+"a"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("URL not found"));
    }
}
