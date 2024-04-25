package com.upwork.urlshortener.hash;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Md5Base64HashTest {

    private final Hash md5Base64Hash = new Md5Base64Hash();


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