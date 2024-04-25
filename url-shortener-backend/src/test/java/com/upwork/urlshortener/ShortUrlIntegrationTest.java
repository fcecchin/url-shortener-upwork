package com.upwork.urlshortener;

import com.upwork.urlshortener.dto.ShortUrlRequest;
import com.upwork.urlshortener.model.ShortUrl;
import com.upwork.urlshortener.repository.ShortUrlRepository;
import com.upwork.urlshortener.service.ShortUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class ShortUrlIntegrationTest extends AbstractSpringIntegrationTest {

    private static final String HASH = "07ecc9";
    private static final String GOOGLE_COM = "https://www.google.com";

    @Autowired
    ShortUrlRepository repository;
    @Autowired
    ShortUrlService service;


    public void cleanAllDatabases() {
        repository.deleteAll();
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

        Optional<ShortUrl> shortUrl = repository.findByKey(HASH);
        assertTrue(shortUrl.isPresent());
        assertNotNull(shortUrl.get().getCreatedAt());
        assertEquals(HASH, shortUrl.get().getKey());
        assertTrue(shortUrl.get().getExpiresAt().isAfter(LocalDateTime.now()));
        assertEquals(GOOGLE_COM, shortUrl.get().getOriginalUrl());
        assertTrue(shortUrl.get().getUrl().endsWith("/" + HASH));
    }

    @Test
    void shouldDeleteShortUrl() throws Exception {
        insertOneUrl();

        this.mockMvc.perform(delete("/{hashed}", HASH))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRedirectShortUrl() throws Exception {
        insertOneUrl();

        this.mockMvc.perform(get("/{hashed}", HASH))
                .andDo(print())
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", GOOGLE_COM));

        repository.findByKey(HASH).ifPresent(url -> {
            assertEquals(1, url.getRedirects());
            assertNotNull(url.getVisitedAt());
        });

    }

    @Test
    void shouldReturnNotFoundWhenRedirectInvalidShortUrl() throws Exception {
        insertOneUrl();
        this.mockMvc.perform(get("/{hashed}", HASH+"a"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("URL not found"));
    }

    @Test
    void shouldIncrementNumberOfRedirects() throws Exception {
        insertOneUrl();
        this.mockMvc.perform(get("/{hashed}", HASH))
                .andDo(print())
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", GOOGLE_COM));

        this.mockMvc.perform(get("/{hashed}", HASH))
                .andDo(print())
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", GOOGLE_COM));

        repository.findByKey(HASH).ifPresent(url -> assertEquals(2, url.getRedirects()));
    }

    private void insertOneUrl() {
        repository.save(ShortUrl.builder()
                .key(HASH)
                .originalUrl(GOOGLE_COM)
                .url("https://localhost/" + HASH)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build());
    }

    @Test
    void testPurgeExpiredUrls() {
        ShortUrl shortUrl = ShortUrl.builder()
                .key(HASH)
                .originalUrl(GOOGLE_COM)
                .url("https://localhost/" + HASH)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        repository.save(shortUrl);
        assertEquals(1, repository.count());
        service.purgeExpiredUrls();
        assertEquals(0, repository.count());
    }

    @Test
    void testCreationSameEncodedUrlShouldCreateOne() throws Exception {
        String body = """
                {
                  "url": "https://www.example.org/demo.php?id=design",
                  "valid-days": 1
                }
                """;
        this.mockMvc.perform(post("/").content(body).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());

        body = """
                {
                  "url": "https://www.example.org/demo.php%3Fid%3Ddesign",
                  "valid-days": 1
                }
                """;

        this.mockMvc.perform(post("/").content(body).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());

        assertEquals(1, repository.count());
    }

    @Test
    void testCreationCustomUrl() throws Exception {
        String body = """
                {
                  "url": "https://www.example.org/",
                  "valid-days": 1,
                  "custom-key": "upwork"
                }
                """;
        this.mockMvc.perform(post("/").content(body).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("upwork"));

        assertEquals(1, repository.count());
    }

    @Test
    void testCreationExistingCustomUrlShouldThrow() throws Exception {
        var customUrl = new ShortUrlRequest("https://www.example.org/", 1, "upwork");
        service.create(customUrl, "http://localhost");

        String body = """
                {
                  "url": "https://www.example.org/",
                  "valid-days": 1,
                  "custom-key": "upwork"
                }
                """;
        this.mockMvc.perform(post("/").content(body).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("409 CONFLICT \"Custom URL already in use\""));

        assertEquals(1, repository.count());}
}
