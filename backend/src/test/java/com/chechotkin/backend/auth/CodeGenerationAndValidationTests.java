package com.chechotkin.backend.auth;

import com.chechotkin.backend.auth.model.LoginToken;
import com.chechotkin.backend.auth.repo.LoginTokenRepo;
import com.chechotkin.backend.auth.service.LoginTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CodeGenerationAndValidationTests {
    CodeGenerator generator;
    LoginTokenRepo loginTokenRepo;
    LoginTokenService sut;

    @BeforeEach
    void setUp() {
        generator = new CodeGenerator();
        loginTokenRepo = new LoginTokenRepoFake();
        sut = new LoginTokenService(generator, loginTokenRepo);
    }



    @Test
    void generateVerificationCode(){

        String verCode = generator.generate();

        assertTrue(verCode.matches("[0-9]{6}"));

    }
    @Test
    void consumeTokenOneTimeTest(){
        String code = generator.generate();
        String token_hash = SHA1Hash.hashString(code);
        String email = "max@gmail.com";
        String sessionId = "abcd1234";
        String requestIp = "142.44.32.104";
        LoginToken token = new LoginToken(token_hash, email , sessionId, requestIp);
        sut.create(token);

        assertTrue(sut.verify(email, code, LocalDateTime.now(), sessionId));

    }
    @Test
    void consumedTokenMoreThanOneTimeTest(){
        String code = generator.generate();
        String token_hash = SHA1Hash.hashString(code);
        String email = "max@gmail.com";
        String sessionId = "abcd1234";
        String requestIp = "142.44.32.104";
        LoginToken token = new LoginToken(token_hash, email , sessionId, requestIp);
        sut.create(token);

        assertTrue(sut.verify(email, code, LocalDateTime.now(), sessionId));
        assertFalse(sut.verify(email, code, LocalDateTime.now(), sessionId));

    }
    @Test
    void shouldRejectOnExpiredTokenTest(){
        String code = generator.generate();
        String token_hash = SHA1Hash.hashString(code);
        String email = "max@gmail.com";
        String sessionId = "abcd1234";
        String requestIp = "142.44.32.104";
        LoginToken token = new LoginToken(token_hash, email , sessionId, requestIp);
        sut.create(token);

        LocalDateTime testTime = LocalDateTime.now().plusMinutes(40);
        assertFalse(sut.verify(email, code, testTime, sessionId));


    }
    @Test
    void shouldRejectOnNewSessionTest(){
        String code = generator.generate();
        String token_hash = SHA1Hash.hashString(code);
        String email = "max@gmail.com";
        String sessionId = "abcd1234";
        String requestIp = "142.44.32.104";
        LoginToken token = new LoginToken(token_hash, email , sessionId, requestIp);
        sut.create(token);

        String testSessionID = "test123";
        assertFalse(sut.verify(email, code, LocalDateTime.now(), testSessionID));

    }
    @Test
    void shouldRejectOnMoreThan3Attempts(){
        String code = generator.generate();
        String token_hash = SHA1Hash.hashString(code);
        String email = "max@gmail.com";
        String sessionId = "abcd1234";
        String requestIp = "142.44.32.104";
        LoginToken token = new LoginToken(token_hash, email , sessionId, requestIp);
        sut.create(token);

        String incorrectCode = "123";

        assertFalse(sut.verify(email, incorrectCode, LocalDateTime.now(), sessionId));
        assertFalse(sut.verify(email, incorrectCode, LocalDateTime.now(), sessionId));
        assertFalse(sut.verify(email, incorrectCode, LocalDateTime.now(), sessionId));
        assertFalse(sut.verify(email, code, LocalDateTime.now(), sessionId));

    }




}
