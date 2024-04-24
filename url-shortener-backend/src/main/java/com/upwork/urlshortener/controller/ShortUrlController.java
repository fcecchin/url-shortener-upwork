package com.upwork.urlshortener.controller;

import com.upwork.urlshortener.annotation.IpRateLimit;
import com.upwork.urlshortener.dto.ShortUrlRequest;
import com.upwork.urlshortener.dto.ShortUrlResponse;
import com.upwork.urlshortener.model.ShortUrl;
import com.upwork.urlshortener.service.ShortUrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8888"})
public class ShortUrlController {

    private final ShortUrlService service;

    public ShortUrlController(ShortUrlService service) {
        this.service = service;
    }

    @IpRateLimit
    @PostMapping
    public ResponseEntity<ShortUrlResponse> create(@Valid @RequestBody ShortUrlRequest requestBody) {
        String context = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        ShortUrlResponse responseBody = service.create(requestBody, context);
        return ResponseEntity.created(URI.create(requestBody.url())).body(responseBody);
    }

    @GetMapping("/{hashed}")
    public ResponseEntity<Void> redirect(@PathVariable String hashed) {
        ShortUrl shortUrl = service.findByHash(hashed);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(shortUrl.getOriginalUrl()));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    @DeleteMapping("/{hashed}")
    public ResponseEntity<Void> delete(@PathVariable String hashed) {
        service.delete(hashed);
        return ResponseEntity.noContent().build();
    }
}
