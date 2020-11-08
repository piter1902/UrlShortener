package urlshortener.service;

import java.nio.charset.StandardCharsets;

import static com.google.common.hash.Hashing.murmur3_32;

public class HashCalculator {

    public static String calculateHash(String url) {
        return murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
    }
}
