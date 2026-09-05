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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class LoginTokenRepoIT extends AbstractIT {

    private static final String EMAIL = "max@gmail.com";
    private static final String SESSION_ID = "session-abc";
    private static final String REQUEST_IP = "142.44.32.104";


    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z").truncatedTo(ChronoUnit.MICROS);

    @Autowired
    private LoginTokenRepo repo;

    @Autowired
    private JdbcClient jdbc;

    @BeforeEach
    void clearTable() {
        jdbc.sql("TRUNCATE login_token RESTART IDENTITY").update();
    }
    private LoginToken issueFor(String email, String session_id, Instant time){
        return LoginToken.issue(CodeHasher.hash("123456", email), email, session_id, REQUEST_IP, time, NOW.plus(15, ChronoUnit.MINUTES));
    }

    @Test
    void insertedTokenIsReadBackUnchangedTest() {
        LoginToken inserted = issueFor(EMAIL, SESSION_ID, NOW);



        repo.insert(inserted);

        Optional<LoginToken> found = repo.findActiveByEmail(EMAIL);

        assertThat(found).isPresent();
        LoginToken token = found.get();


        assertThat(token.id()).isNotNull();


        assertThat(token.tokenHash()).isEqualTo(inserted.tokenHash());
        assertThat(token.email()).isEqualTo(EMAIL);
        assertThat(token.sessionId()).isEqualTo(SESSION_ID);
        assertThat(token.requestIp()).isEqualTo(REQUEST_IP);
        assertThat(token.createdAt()).isEqualTo(NOW);
        assertThat(token.expiresAt()).isEqualTo(NOW.plus(15, ChronoUnit.MINUTES));


        assertThat(token.attempts()).isZero();
        assertThat(token.consumedAt()).isNull();
    }
    @Test
    void getOneValidTokenTest(){
        LoginToken inserted1 = issueFor(EMAIL, SESSION_ID, NOW);

        repo.insert(inserted1);

        Instant testTime = NOW.plus(15, ChronoUnit.MINUTES);
        String testSession = "test-session";

        LoginToken inserted2 = issueFor(EMAIL, testSession, testTime);


        repo.insert(inserted2);

        Optional<LoginToken> found = repo.findActiveByEmail(EMAIL);

        assertThat(found).isPresent();

        LoginToken token = found.get();

        assertEquals("test-session", token.sessionId());

    }

    @Test
    void removesUnconsumedTokenTest(){
        repo.insert(issueFor(EMAIL, SESSION_ID, NOW));
        repo.insert(issueFor("other@gmail.com", SESSION_ID, NOW));

        repo.insert(issueFor(EMAIL, SESSION_ID, NOW));
        long consumedId = repo.findActiveByEmail(EMAIL).orElseThrow().id();
        repo.consume(consumedId, NOW);

        int deleted = repo.deleteActiveFor(EMAIL);

        assertThat(deleted).isEqualTo(1);
        assertThat(repo.findActiveByEmail(EMAIL)).isEmpty();
        assertThat(repo.findActiveByEmail("other@gmail.com")).isPresent();

        Long surviving = jdbc.sql("SELECT count(*) FROM login_token WHERE email = :e")
                .param("e", EMAIL).query(Long.class).single();
        assertThat(surviving).isEqualTo(1);

    }

    @Test
    void shouldIncrementAttemptsTest(){
        LoginToken inserted = issueFor(EMAIL, SESSION_ID, NOW);

        repo.insert(inserted);

        LoginToken token = repo.findActiveByEmail(EMAIL).get();

        assertThat(repo.incrementAttempts(token.id())).isEqualTo(1);
        assertThat(repo.incrementAttempts(token.id())).isEqualTo(2);



    }

    @Test
    void shouldRejectOnConsumedToken(){

        LoginToken inserted = issueFor(EMAIL, SESSION_ID, NOW);

        repo.insert(inserted);

        LoginToken token = repo.findActiveByEmail(EMAIL).get();

        assertTrue(repo.consume(token.id(), NOW));

        Instant consumedAt = jdbc.sql("SELECT consumed_at FROM login_token WHERE id = :id")
                .param("id", token.id())
                .query(Instant.class)
                .single();

        assertThat(consumedAt).isEqualTo(NOW);
        assertFalse(repo.consume(token.id(), NOW));

    }

    @Test
    void shouldRemoveOldTokensTest(){
        LoginToken inserted = issueFor(EMAIL, SESSION_ID, NOW);

        repo.insert(inserted);

        Instant testTime1 = NOW.minus(15, ChronoUnit.DAYS);

        repo.deleteOlderThan(testTime1);

        Optional<LoginToken> token1 = repo.findActiveByEmail(EMAIL);

        assertThat(token1).isPresent();

        Instant testTime2 = NOW.plus(15, ChronoUnit.DAYS);

        repo.deleteOlderThan(testTime2);

        Optional<LoginToken> token2 = repo.findActiveByEmail(EMAIL);

        assertThat(token2).isNotPresent();

    }
    @Test
    void onlyOneOfTwoConcurrentConsumeWins() throws Exception{
        repo.insert(issueFor(EMAIL, SESSION_ID, NOW));
        long id = repo.findActiveByEmail(EMAIL).orElseThrow().id();

        var latch = new CountDownLatch(1); // helps to start threads together
        var pool = Executors.newFixedThreadPool(2); //  calling two consumes on different threads

        Callable<Boolean> attempt = () -> { // both threads block on the latch, then race
            latch.await();
            return repo.consume(id, NOW);
        };

        // Submit both before releasing the latch
        Future<Boolean> first  = pool.submit(attempt);
        Future<Boolean> second = pool.submit(attempt);


        latch.countDown(); //Release latch

        boolean a = first.get();
        boolean b = second.get();

        assertThat(List.of(a, b)).containsExactlyInAnyOrder(true, false);

    }



}
