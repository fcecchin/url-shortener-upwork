package com.upwork.urlshortener.service;

import com.upwork.urlshortener.dto.ShortUrlRequest;
import com.upwork.urlshortener.dto.ShortUrlResponse;
import com.upwork.urlshortener.exception.InvalidUrlException;
import com.upwork.urlshortener.hash.Hash;
import com.upwork.urlshortener.model.ShortUrl;
import com.upwork.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    private static final String HASH = "qwerty";
    private static final String CONTEXT = "http://localhost:8080";
    @InjectMocks
    private ShortUrlService service;

    @Mock
    private ShortUrlRepository repository;

    @Mock
    private Hash hashAlgo;
    private ShortUrl shortUrl;

    @BeforeEach
    void setUp() {
        shortUrl = ShortUrl.builder()
                .key(HASH)
                .originalUrl("https://google.com")
                .url(CONTEXT + "/" + HASH)
                .build();
    }

    @Test
    void testCreation() {
        String originalUrl = "https://google.com";
        when(hashAlgo.hash(originalUrl)).thenReturn(HASH);
        when(repository.save(any(ShortUrl.class))).thenReturn(shortUrl);

        ShortUrlResponse response = service.create(new ShortUrlRequest(originalUrl, 1), CONTEXT);

        assertEquals(originalUrl, response.originalUrl());
        assertEquals(HASH, response.key());
        assertEquals(CONTEXT + "/" + HASH, response.shortUrl());
        verify(repository, times(1)).save(any(ShortUrl.class));
    }

    @Test
    void testCreationInvalidUrl() {
        String originalUrl = "google.com";
        ShortUrlRequest request = new ShortUrlRequest(originalUrl, 1);
        assertThrows(InvalidUrlException.class, () -> service.create(request, CONTEXT));
    }

    @Test
    void testFindByHash() {
        when(repository.findByKey(HASH)).thenReturn(Optional.of(shortUrl));
        ShortUrl url = service.findByHash(HASH);
        assertEquals("https://google.com", url.getOriginalUrl());
        verify(repository, times(1)).findByKey(HASH);
    }

    @Test
    void testDelete() {
        when(repository.findByKey(HASH)).thenReturn(Optional.of(shortUrl));
        service.delete(HASH);
        verify(repository, times(1)).delete(shortUrl);
    }

    @Test
    void testDeleteExpired() {
        service.purgeExpiredUrls();
        verify(repository, times(1)).deleteAllExpired(any(LocalDateTime.class));
    }
}