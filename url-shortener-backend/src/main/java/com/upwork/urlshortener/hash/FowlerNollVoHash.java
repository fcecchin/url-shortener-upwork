package com.upwork.urlshortener.hash;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Primary
@Component
public class FowlerNollVoHash implements Hash {
    private static final BigInteger FNV_OFFSET_BASIS = BigInteger.valueOf(2166136261L);
    private static final BigInteger FNV_PRIME = BigInteger.valueOf(16777619L);
    @Override
    public String hash(String value) {
        BigInteger hsh = FNV_OFFSET_BASIS;

        byte[] data = value.getBytes();

        for (Byte d: data) {
            hsh = hsh.xor(new BigInteger(d.toString()));
            hsh = hsh.multiply(FNV_PRIME);
        }
        int pos = value.length();
        return hsh.toString(16).substring(pos, pos + 6);
    }
}
