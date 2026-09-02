package com.chechotkin.backend.auth;

import com.chechotkin.backend.auth.repo.LoginTokenRepo;
import com.chechotkin.backend.auth.service.LoginTokenService;
import com.chechotkin.backend.auth.service.VerifyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class CodeGenerationAndValidationTests {

    private static final String EMAIL = "max@gmail.com";
    private static final String SESSION_ID = "abcd1234";
    private static final String REQUEST_IP = "142.44.32.104";
    private static final Instant START = Instant.parse("2026-01-01T12:00:00Z");

    CodeGenerator generator;
    LoginTokenRepo loginTokenRepo;
    MutableClock clock;
    LoginTokenService sut;

    @BeforeEach
    void setUp() {
        generator = new CodeGenerator();
        loginTokenRepo = new LoginTokenRepoFake();
        clock = new MutableClock(START, ZoneOffset.UTC);
        sut = new LoginTokenService(generator, loginTokenRepo, clock);
    }

    private String issueCodeFor(String email, String sessionId) {
        return sut.create(email, sessionId, REQUEST_IP);
    }

    @Test
    void generateVerificationCode(){

        String verCode = generator.generate();

        assertTrue(verCode.matches("[0-9]{6}"));

    }
    @Test
    void consumeTokenOneTimeTest(){
        String code = issueCodeFor(EMAIL, SESSION_ID);

        assertEquals(VerifyResult.OK, sut.verify(EMAIL, code, SESSION_ID));

    }
    @Test
    void consumedTokenMoreThanOneTimeTest(){
        String code = issueCodeFor(EMAIL, SESSION_ID);

        assertEquals(VerifyResult.OK, sut.verify(EMAIL, code, SESSION_ID));
        assertEquals(VerifyResult.CONSUMED, sut.verify(EMAIL, code, SESSION_ID));

    }
    @Test
    void shouldRejectOnExpiredTokenTest(){
        String code = issueCodeFor(EMAIL, SESSION_ID);

        clock.advance(Duration.ofMinutes(40));

        assertEquals(VerifyResult.EXPIRED, sut.verify(EMAIL, code, SESSION_ID));

    }
    @Test
    void shouldAcceptOneSecondBeforeExpiryTest(){
        String code = issueCodeFor(EMAIL, SESSION_ID);

        clock.advance(Duration.ofMinutes(15).minusSeconds(1));

        assertEquals(VerifyResult.OK, sut.verify(EMAIL, code, SESSION_ID));

    }
    @Test
    void shouldRejectOneSecondAfterExpiryTest(){
        String code = issueCodeFor(EMAIL, SESSION_ID);

        clock.advance(Duration.ofMinutes(15).plusSeconds(1));

        assertEquals(VerifyResult.EXPIRED, sut.verify(EMAIL, code, SESSION_ID));

    }
    @Test
    void shouldRejectOnNewSessionTest(){
        String code = issueCodeFor(EMAIL, SESSION_ID);

        assertEquals(VerifyResult.WRONG_SESSION, sut.verify(EMAIL, code, "test123"));

    }
    @Test
    void shouldRejectOnSingleWrongCodeTest(){
        issueCodeFor(EMAIL, SESSION_ID);

        assertEquals(VerifyResult.WRONG_CODE, sut.verify(EMAIL, "123", SESSION_ID));

    }
    @Test
    void shouldRejectOnUnknownEmailTest(){
        assertEquals(VerifyResult.WRONG_CODE, sut.verify("nobody@gmail.com", "123456", SESSION_ID));

    }
    @Test
    void shouldRejectOnMoreThan3Attempts(){
        String code = issueCodeFor(EMAIL, SESSION_ID);

        String incorrectCode = "123";

        assertEquals(VerifyResult.WRONG_CODE, sut.verify(EMAIL, incorrectCode, SESSION_ID));
        assertEquals(VerifyResult.WRONG_CODE, sut.verify(EMAIL, incorrectCode, SESSION_ID));
        assertEquals(VerifyResult.TOO_MANY_ATTEMPTS, sut.verify(EMAIL, code, SESSION_ID));

    }
}
