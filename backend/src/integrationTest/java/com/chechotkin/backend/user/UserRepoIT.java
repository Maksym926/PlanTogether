package com.chechotkin.backend.user;

import com.chechotkin.backend.AbstractIT;
import com.chechotkin.backend.user.model.User;
import com.chechotkin.backend.user.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserRepoIT extends AbstractIT {
    private final String EMAIL = "max@gmail.com";
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z").truncatedTo(ChronoUnit.MICROS);

    @Autowired
    private UserRepo users;

    @Test
    void shouldCreateUserIfNotExistsOtherwiseUpdateUserTest(){
        users.upsertByEmail(EMAIL, NOW);
        User found1 = users.getByEmail(EMAIL).get();

        assertEquals("max@gmail.com", found1.email());
        assertEquals("max@gmail.com", found1.displayed_name());

        Instant testTime = NOW.plus(15, ChronoUnit.DAYS);
        users.upsertByEmail(EMAIL, testTime);
        Optional<User> found2 = users.getByEmail(EMAIL);

        assertTrue(found2.isPresent());

        assertEquals(testTime, found2.get().created_at());


    }
}
