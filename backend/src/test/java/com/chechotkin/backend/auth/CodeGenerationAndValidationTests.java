package com.chechotkin.backend.auth;

import com.chechotkin.backend.auth.model.LoginToken;
import com.chechotkin.backend.auth.repo.LoginTokenRepo;
import com.chechotkin.backend.auth.service.LoginTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void consumeCodeOneTimeTest(){
        String code = generator.generate();
        String token_hash = SHA1Hash.hashString(code);
        String email = "max@gmail.com";
        String sessionId = "abcd1234";
        String requestIp = "142.44.32.104";
        LoginToken token = new LoginToken(token_hash, email , sessionId, requestIp);
        sut.create(token);

        assertTrue(sut.verify(email, code));

    }
    @Test
    void consumedCodeMoreThanOneTimeTest(){
        String code = generator.generate();
        String token_hash = SHA1Hash.hashString(code);
        String email = "max@gmail.com";
        String sessionId = "abcd1234";
        String requestIp = "142.44.32.104";
        LoginToken token = new LoginToken(token_hash, email , sessionId, requestIp);
        sut.create(token);

        assertTrue(sut.verify(email, code));
        assertFalse(sut.verify(email, code));





    }


}
