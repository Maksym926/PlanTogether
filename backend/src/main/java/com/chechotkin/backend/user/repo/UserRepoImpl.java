package com.chechotkin.backend.user.repo;

import com.chechotkin.backend.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Repository
public class UserRepoImpl implements UserRepo{


    private final JdbcClient jdbc;

    public UserRepoImpl(JdbcClient jdbc){
        this.jdbc = jdbc;
    }

    @Override
    public User upsertByEmail(String email, Instant createdAt) {
        return jdbc.sql("""
                INSERT INTO users (email, displayed_name, created_at)
                VALUES (:email, :displayed_name, :created_at)
                ON CONFLICT (lower(email))
                DO UPDATE SET displayed_name = EXCLUDED.displayed_name
                RETURNING *
                """)
                .param("email", email)
                .param("displayed_name", email)
                .param("created_at",  OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC))
                .query(User.class)
                .single();
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return jdbc.sql("""
                    SELECT id, email, displayed_name, created_at
                    FROM users
                    WHERE lower(email) = lower(:email)
                    """)
                .param("email", email)
                .query(User.class)
                .optional();
    }
}
