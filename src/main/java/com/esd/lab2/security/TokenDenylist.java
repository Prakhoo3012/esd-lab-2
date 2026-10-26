package com.esd.lab2.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenDenylist {

    // key = jti, value = the token's own expiry
    private final ConcurrentHashMap<String, Instant> revoked = new ConcurrentHashMap<>();

    public void revoke(String jti, Instant expiry) {
        cleanExpired();
        revoked.put(jti, expiry);
    }

    public boolean isRevoked(String jti) {
        return revoked.containsKey(jti);
    }

    public void cleanExpired() {
        Instant now = Instant.now();
        revoked.entrySet().removeIf(e -> e.getValue().isBefore(now));
    }
}