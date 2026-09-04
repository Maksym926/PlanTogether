package com.chechotkin.backend.auth;

import com.chechotkin.backend.AbstractIT;
import com.chechotkin.backend.auth.model.LoginToken;
import com.chechotkin.backend.auth.repo.LoginTokenRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class LoginTokenRepoImplIT extends AbstractIT {

    private static final String EMAIL = "max@gmail.com";
    private static final String SESSION_ID = "session-abc";
    private static final String REQUEST_IP = "142.44.32.104";

    // Postgres timestamptz keeps microseconds; Instant keeps nanoseconds. Truncate
    // the fixture, or a value that prints identically will fail an equality assert.
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z").truncatedTo(ChronoUnit.MICROS);

    @Autowired
    private LoginTokenRepo repo;

    @Autowired
    private JdbcClient jdbc;

    @BeforeEach
    void clearTable() {
        // Not @Transactional: rolling back would hide rows from other threads,
        // which breaks the concurrency test for consume() later in this class.
        jdbc.sql("TRUNCATE login_token RESTART IDENTITY").update();
    }

    @Test
    void insertedTokenIsReadBackUnchangedTest() {
        LoginToken inserted = LoginToken.issue(
                CodeHasher.hash("123456", EMAIL),
                EMAIL,
                SESSION_ID,
                REQUEST_IP,
                NOW,
                NOW.plus(15, ChronoUnit.MINUTES));

        repo.insert(inserted);

        Optional<LoginToken> found = repo.findActiveByEmail(EMAIL);

        assertThat(found).isPresent();
        LoginToken token = found.get();

        // The id is assigned by the database, so it exists here but not on the
        // object we passed in.
        assertThat(token.id()).isNotNull();

        // Every column, because this is what proves SimplePropertyRowMapper really
        // maps token_hash -> tokenHash, created_at -> createdAt, and so on. Any
        // one of these silently arriving null is the failure mode worth catching.
        assertThat(token.tokenHash()).isEqualTo(inserted.tokenHash());
        assertThat(token.email()).isEqualTo(EMAIL);
        assertThat(token.sessionId()).isEqualTo(SESSION_ID);
        assertThat(token.requestIp()).isEqualTo(REQUEST_IP);
        assertThat(token.createdAt()).isEqualTo(NOW);
        assertThat(token.expiresAt()).isEqualTo(NOW.plus(15, ChronoUnit.MINUTES));

        // Both come from column defaults — insert() never sends them.
        assertThat(token.attempts()).isZero();
        assertThat(token.consumedAt()).isNull();
    }
    @Test
    void getOneValidTokenTest(){
        LoginToken inserted1 = LoginToken.issue(
                CodeHasher.hash("123456", EMAIL),
                EMAIL,
                SESSION_ID,
                REQUEST_IP,
                NOW,
                NOW.plus(15, ChronoUnit.MINUTES));

        repo.insert(inserted1);

        Instant testTime = NOW.plus(15, ChronoUnit.MINUTES);
        String testSession = "test-session";

        LoginToken inserted2 = LoginToken.issue(
                CodeHasher.hash("123456", EMAIL),
                EMAIL,
                testSession,
                REQUEST_IP,
                testTime,
                NOW.plus(15, ChronoUnit.MINUTES));

        repo.insert(inserted2);

        Optional<LoginToken> found = repo.findActiveByEmail(EMAIL);

        assertThat(found).isPresent();

        LoginToken token = found.get();

        assertEquals("test-session", token.sessionId());

    }

    @Test
    void removesUnconsumedTokenTest(){
        LoginToken inserted1 = LoginToken.issue(
                CodeHasher.hash("123456", EMAIL),
                EMAIL,
                SESSION_ID,
                REQUEST_IP,
                NOW,
                NOW.plus(15, ChronoUnit.MINUTES));

        repo.insert(inserted1);

        repo.deleteActiveFor(EMAIL);

        Optional<LoginToken> found = repo.findActiveByEmail(EMAIL);

        assertThat(found).isNotPresent();

    }

    @Test
    void shouldIncrementAttemptsTest(){
        LoginToken inserted1 = LoginToken.issue(
                CodeHasher.hash("123456", EMAIL),
                EMAIL,
                SESSION_ID,
                REQUEST_IP,
                NOW,
                NOW.plus(15, ChronoUnit.MINUTES));

        repo.insert(inserted1);

        LoginToken foundWith0Attempts = repo.findActiveByEmail(EMAIL).get();

        repo.incrementAttempts(foundWith0Attempts.id());

        LoginToken foundWith1Attempt = repo.findActiveByEmail(EMAIL).get();

        assertEquals(1, foundWith1Attempt.attempts());

    }

    @Test
    void shouldRejectOnConsumedToken(){

        LoginToken inserted1 = LoginToken.issue(
                CodeHasher.hash("123456", EMAIL),
                EMAIL,
                SESSION_ID,
                REQUEST_IP,
                NOW,
                NOW.plus(15, ChronoUnit.MINUTES));

        repo.insert(inserted1);

        LoginToken token = repo.findActiveByEmail(EMAIL).get();

        assertTrue(repo.consume(token.id(), NOW));

        LoginToken consumedToken = repo.findActiveByEmail(EMAIL).get();

        assertNotNull(consumedToken.consumedAt());
        assertFalse(repo.consume(token.id(), NOW));

    }

    @Test
    void shouldRemoveOldTokensTest(){
        LoginToken inserted1 = LoginToken.issue(
                CodeHasher.hash("123456", EMAIL),
                EMAIL,
                SESSION_ID,
                REQUEST_IP,
                NOW,
                NOW.plus(15, ChronoUnit.MINUTES));

        repo.insert(inserted1);

        Instant testTime1 = NOW.minus(15, ChronoUnit.DAYS);

        repo.deleteOlderThan(testTime1);

        Optional<LoginToken> token1 = repo.findActiveByEmail(EMAIL);

        assertThat(token1).isPresent();

        Instant testTime2 = NOW.plus(15, ChronoUnit.DAYS);

        repo.deleteOlderThan(testTime2);

        Optional<LoginToken> token2 = repo.findActiveByEmail(EMAIL);

        assertThat(token2).isNotPresent();




    }



}
