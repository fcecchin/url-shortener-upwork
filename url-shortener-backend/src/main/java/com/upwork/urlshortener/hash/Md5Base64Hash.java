package com.upwork.urlshortener.hash;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import static java.security.MessageDigest.getInstance;

@Component
public class Md5Base64Hash implements Hash {

    @Override
    public String hash(String value)  {
        //decode URL encoded string
        value = URLDecoder.decode(value, StandardCharsets.UTF_8) + LocalDateTime.now().getLong(ChronoField.EPOCH_DAY);
        try {
            return new BigInteger(1, getInstance("MD5")
                    .digest(value.getBytes(StandardCharsets.UTF_8))).toString(16).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            return value;
        }
    }
}
