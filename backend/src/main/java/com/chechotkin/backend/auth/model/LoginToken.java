package com.chechotkin.backend.auth.model;

import java.time.Instant;


public record   LoginToken(
        Long id,
        String tokenHash,
        String email,
        String sessionId,
        int attempts,
        Instant createdAt,
        Instant expiresAt,
        Instant consumedAt,
        String requestIp) {


    public static LoginToken issue(String tokenHash, String email, String sessionId, String requestIp,
                                   Instant createdAt, Instant expiresAt) {
        return new LoginToken(null, tokenHash, email, sessionId, 0, createdAt, expiresAt, null, requestIp);
    }

    public LoginToken withId(long newId) {
        return new LoginToken(newId, tokenHash, email, sessionId, attempts, createdAt, expiresAt, consumedAt, requestIp);
    }

    public LoginToken withAttempts(int newAttempts) {
        return new LoginToken(id, tokenHash, email, sessionId, newAttempts, createdAt, expiresAt, consumedAt, requestIp);
    }

    public LoginToken withConsumedAt(Instant at) {
        return new LoginToken(id, tokenHash, email, sessionId, attempts, createdAt, expiresAt, at, requestIp);
    }
}
