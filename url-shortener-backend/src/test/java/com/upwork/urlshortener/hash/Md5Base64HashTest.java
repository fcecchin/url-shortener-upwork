package com.upwork.urlshortener.hash;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class Md5Base64HashTest {

    private final Hash md5Base64Hash = new Md5Base64Hash();

    @Test
    void testHashForSameUrlEncodedShouldBeSame() {
        String plainUrl = md5Base64Hash.hash("https://www.example.org/demo.php?id=design");
        String encodedUrl = md5Base64Hash.hash("https://www.example.org/demo.php%3Fid%3Ddesign");
        assertEquals(plainUrl, encodedUrl);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.google.com",
            "http",
            "example.com",
            "a.au",
            "https://www.exampleverylooooooongthatisincredibleweird.org/demo.php?id=design"
    })
    void testHashForDifferentUrlShouldHaveSameLength(String url) {
        String shortUrl = md5Base64Hash.hash(url);
        assertEquals(8, shortUrl.length());
    }
}