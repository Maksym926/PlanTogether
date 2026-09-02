package com.chechotkin.backend.auth.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LoginToken {

    private String token_hash;

    private String email;

    private String sessionId;

    private Integer attempts;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime consumedAt;

    private String requestIp;

    public LoginToken(String token_hash, String email, String sessionId, String requestIp) {
        this.token_hash = token_hash;
        this.email = email;
        this.sessionId = sessionId;
        this.requestIp = requestIp;
        attempts = 0;
        createdAt = LocalDateTime.now();
        expiresAt = createdAt.plusMinutes(15);


    }
}
