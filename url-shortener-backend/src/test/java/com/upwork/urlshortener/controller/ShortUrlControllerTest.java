package com.upwork.urlshortener.controller;

import com.upwork.urlshortener.dto.ShortUrlRequest;
import com.upwork.urlshortener.dto.ShortUrlResponse;
import com.upwork.urlshortener.model.ShortUrl;
import com.upwork.urlshortener.service.ShortUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ShortUrlController.class)
class ShortUrlControllerTest {

    private static final String HASH = "qwerty";
    private static final String CONTEXT = "http://localhost:8080";
    private static final String ORIGINAL_URL = "https://google.com";
    private static final String VALID_REQUEST_JSON = """
            {
                "url": "https://google.com",
                "valid-days": 1
            }
            """;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    private ShortUrlService shortUrlService;
    private ShortUrlResponse response;

    @BeforeEach
    void setUp() {
        response = new ShortUrlResponse(ORIGINAL_URL, HASH, CONTEXT + "/" + HASH);
    }

    @Test
    void testCreateWithSuccess() throws Exception {
        when(shortUrlService.create(any(ShortUrlRequest.class), any(String.class))).thenReturn(response);
        this.mockMvc.perform(post("/")
                        .content(VALID_REQUEST_JSON)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.original_url").value(response.originalUrl()))
                .andExpect(jsonPath("$.short_url").value(response.shortUrl()))
                .andExpect(jsonPath("$.key").value(response.key()));
    }

    @Test
    void testCreateInvalidUrl() throws Exception {
        when(shortUrlService.create(any(ShortUrlRequest.class), any(String.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        this.mockMvc.perform(post("/")
                        .content("""
                                {
                                    "url": "www. google.com",
                                    "valid-days": 1
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateInvalidJson() throws Exception {
        this.mockMvc.perform(post("/")
                        .content("""
                                {
                                    "url-wrong": "www. google.com"
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testRedirectUrlWithSuccess() throws Exception {
        when(shortUrlService.redirect(HASH)).thenReturn(ShortUrl.builder()
                .originalUrl(ORIGINAL_URL)
                .url(CONTEXT + "/" + HASH)
                .redirects(1L)
                .key(HASH)
                .build());
        this.mockMvc.perform(get("/{hashed}", HASH))
                .andExpect(status().isMovedPermanently())
                .andExpect(header()
                        .stringValues("Location", ORIGINAL_URL));
    }

    @Test
    void testDeleteWithSuccess() throws Exception {
        this.mockMvc.perform(delete("/{hashed}", HASH))
                .andExpect(status().isNoContent());
    }
}