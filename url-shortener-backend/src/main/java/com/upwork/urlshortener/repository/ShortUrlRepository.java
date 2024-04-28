package com.upwork.urlshortener.repository;

import com.upwork.urlshortener.model.ShortUrl;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Update;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ShortUrlRepository extends MongoRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByKey(String key);

    @DeleteQuery("{'expiresAt': {$lt: ?0}}")
    void deleteAllExpired(LocalDateTime date);

    @Update("{ '$inc' : { 'redirects' : 1}, '$set' : { 'visitedAt' : ?1} }")
    void findAndIncrementVisitsByKey(String key, LocalDateTime date);
}
