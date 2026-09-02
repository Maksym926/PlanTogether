package com.chechotkin.backend.auth.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class LoginToken {

    /** null until insert() assigns one. */
    private Long id;

    private String token_hash;

    private String email;

    private String sessionId;

    private Integer attempts;

    private Instant createdAt;

    private Instant expiresAt;

    private Instant consumedAt;

    private String requestIp;

    public LoginToken(String token_hash, String email, String sessionId, String requestIp,
                      Instant createdAt, Instant expiresAt) {
        this.token_hash = token_hash;
        this.email = email;
        this.sessionId = sessionId;
        this.requestIp = requestIp;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        attempts = 0;
    }
}
