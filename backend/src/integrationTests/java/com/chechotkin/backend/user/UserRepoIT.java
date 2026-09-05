package com.chechotkin.backend.user;

import com.chechotkin.backend.AbstractIT;
import com.chechotkin.backend.user.model.User;
import com.chechotkin.backend.user.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepoIT extends AbstractIT {

    private static final String EMAIL = "max@gmail.com";
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z").truncatedTo(ChronoUnit.MICROS);

    @Autowired
    private UserRepo users;

    @Autowired
    private JdbcClient jdbc;

    @BeforeEach
    void clearTable() {

        jdbc.sql("TRUNCATE users RESTART IDENTITY").update();
    }

    private long userCount() {
        return jdbc.sql("SELECT count(*) FROM users").query(Long.class).single();
    }

    @Test
    void firstUpsertCreatesTheUser() {
        User created = users.upsertByEmail(EMAIL, NOW);

        assertThat(created.id()).isNotNull();
        assertThat(created.email()).isEqualTo(EMAIL);
        assertThat(created.displayedName()).isEqualTo(EMAIL);
        assertThat(created.createdAt()).isEqualTo(NOW);

        assertThat(users.getByEmail(EMAIL)).contains(created);
    }

    @Test
    void secondUpsertUpdatesInsteadOfInserting() {
        users.upsertByEmail(EMAIL, NOW);
        long idAfterFirst = users.getByEmail(EMAIL).orElseThrow().id();

        users.upsertByEmail(EMAIL, NOW.plus(15, ChronoUnit.DAYS));


        assertThat(userCount()).isEqualTo(1);
        assertThat(users.getByEmail(EMAIL).orElseThrow().id()).isEqualTo(idAfterFirst);
    }

    @Test
    void secondUpsertKeepsTheOriginalCreationTime() {
        users.upsertByEmail(EMAIL, NOW);

        users.upsertByEmail(EMAIL, NOW.plus(15, ChronoUnit.DAYS));


        assertThat(users.getByEmail(EMAIL).orElseThrow().createdAt()).isEqualTo(NOW);
    }

    @Test
    void emailMatchingIgnoresCase() {
        users.upsertByEmail(EMAIL, NOW);

        assertThat(users.getByEmail("MAX@Gmail.COM")).isPresent();


        users.upsertByEmail("Max@Gmail.com", NOW.plus(1, ChronoUnit.DAYS));
        assertThat(userCount()).isEqualTo(1);
    }

    @Test
    void unknownEmailReturnsEmpty() {
        assertThat(users.getByEmail("nobody@gmail.com")).isEmpty();
    }
}
