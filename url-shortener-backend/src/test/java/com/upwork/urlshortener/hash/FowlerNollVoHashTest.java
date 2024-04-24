package com.upwork.urlshortener.hash;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FowlerNollVoHashTest {

    private final FowlerNollVoHash fowlerNollVoHash = new FowlerNollVoHash();

    @Test
    void testHashForSameUrlShouldBeSame() {
        String url = "https://www.google.com";
        String hashed1 = fowlerNollVoHash.hash(url);
        String hashed2 = fowlerNollVoHash.hash(url);
        assertEquals(6, hashed1.length());
        assertEquals(hashed1, hashed2);
    }

    @Test
    void testHashForSameUrlEncodedShouldBeSame() {
        String plainUrl = fowlerNollVoHash.hash("https://www.example.org/demo.php?id=design");
        String encodedUrl = fowlerNollVoHash.hash("https://www.example.org/demo.php%3Fid%3Ddesign");
        assertEquals(plainUrl, encodedUrl);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.google.com", "http", "example.com", "a.au"
    })
    void testHashForDifferentUrlShouldHaveSameLength(String url) {
        String shortUrl = fowlerNollVoHash.hash(url);
        assertEquals(6, shortUrl.length());
    }
}