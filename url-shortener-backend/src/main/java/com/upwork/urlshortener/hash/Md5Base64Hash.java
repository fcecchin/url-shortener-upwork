package com.upwork.urlshortener.hash;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import static java.security.MessageDigest.getInstance;

@Component
public class Md5Base64Hash implements Hash {

    @Override
    public String hash(String value)  {
        try {
            return new BigInteger(1, getInstance("MD5")
                    .digest(value.getBytes(StandardCharsets.UTF_8))).toString(16).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            return value;
        }
    }
}
