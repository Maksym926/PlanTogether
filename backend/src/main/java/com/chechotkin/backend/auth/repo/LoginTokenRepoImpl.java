package com.chechotkin.backend.auth.repo;

import com.chechotkin.backend.auth.model.LoginToken;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Repository
public class LoginTokenRepoImpl implements LoginTokenRepo{

    private final JdbcClient jdbc;

    public LoginTokenRepoImpl(JdbcClient jdbc){
        this.jdbc = jdbc;
    }

    @Override
    public void insert(LoginToken token) {
        jdbc.sql("""
        INSERT INTO login_token (token_hash, email, session_id, created_at, expires_at, request_ip)
        VALUES (:token_hash, :email, :session_id, :created_at, :expires_at, :request_ip)
        """)
                .param("token_hash", token.tokenHash())
                .param("email", token.email())
                .param("session_id", token.sessionId())
                .param("created_at", OffsetDateTime.ofInstant(token.createdAt(), ZoneOffset.UTC))
                .param("expires_at", OffsetDateTime.ofInstant(token.expiresAt(), ZoneOffset.UTC))
                .param("request_ip", token.requestIp())
                .update();
    }

    @Override
    public Optional<LoginToken> findActiveByEmail(String email) {
        return jdbc.sql("""
                        SELECT id, token_hash, email, session_id, attempts,
                               created_at, expires_at, consumed_at, request_ip
                        FROM login_token
                        WHERE email = :email
                        ORDER BY created_at DESC, id DESC
                        LIMIT 1
                        """)
                            .param("email", email)
                            .query(LoginToken.class)
                            .optional();
    }

    @Override
    public int deleteActiveFor(String email) {
        return jdbc.sql("DELETE From login_token WHERE email=:email AND consumed_at IS NULL")
                .param("email", email)
                .update();
    }

    @Override
    public int incrementAttempts(long id) {
        return jdbc.sql("""
            UPDATE login_token SET attempts = attempts + 1
            WHERE id = :id
            RETURNING attempts
            """)
                .param("id", id)
                .query(Integer.class)
                .single();
    }

    @Override
    public boolean consume(long id, Instant at) {
        return jdbc.sql("""
                UPDATE login_token SET consumed_at=:consumed_at
                WHERE id=:id AND consumed_at IS NULL
            """)
                .param("consumed_at", OffsetDateTime.ofInstant(at, ZoneOffset.UTC))
                .param("id", id)
                .update() == 1;

    }

    @Override
    public int deleteOlderThan(Instant cutoff) {
        return jdbc.sql("DELETE From login_token WHERE created_at<:cutoff")
                .param("cutoff", OffsetDateTime.ofInstant(cutoff, ZoneOffset.UTC))
                .update();
    }
}
