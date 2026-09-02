package com.chechotkin.backend.auth.service;

import com.chechotkin.backend.auth.CodeGenerator;
import com.chechotkin.backend.auth.CodeHasher;
import com.chechotkin.backend.auth.model.LoginToken;
import com.chechotkin.backend.auth.repo.LoginTokenRepo;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class LoginTokenService {

    private static final Duration TIME_TO_LIVE = Duration.ofMinutes(15);

    private final CodeGenerator generator;
    private final LoginTokenRepo loginTokenRepo;
    private final Clock clock;

    public LoginTokenService(CodeGenerator generator, LoginTokenRepo loginTokenRepo, Clock clock) {
        this.generator = generator;
        this.loginTokenRepo = loginTokenRepo;
        this.clock = clock;
    }

    public String create(String email, String sessionId, String requestIp) {
        String code = generator.generate();
        Instant now = clock.instant();
        LoginToken token = new LoginToken(CodeHasher.hash(code, email), email, sessionId, requestIp,
                now, now.plus(TIME_TO_LIVE));
        loginTokenRepo.put(email, token);
        return code;
    }

    public VerifyResult verify(String email, String code, String sessionId) {
        String hashedCode = CodeHasher.hash(code, email);
        LoginToken token;
        if(loginTokenRepo.get(email).isPresent()){
            token = loginTokenRepo.get(email).get();
        }
        else {
            return VerifyResult.WRONG_CODE;
        }

        Instant now = clock.instant();
        token.setAttempts(token.getAttempts() + 1);
        loginTokenRepo.put(email, token);
        if(token.getConsumedAt() != null){
            return VerifyResult.CONSUMED;
        }
        if(token.getAttempts() >= 3){
            return VerifyResult.TOO_MANY_ATTEMPTS;
        }
        if(now.isAfter(token.getExpiresAt())){
            return VerifyResult.EXPIRED;
        }
        if(!sessionId.equals(token.getSessionId())){
            return VerifyResult.WRONG_SESSION;
        }
        if(!hashedCode.equals(token.getToken_hash())){
            return VerifyResult.WRONG_CODE;
        }
        token.setConsumedAt(now);
        loginTokenRepo.put(email, token);
        return VerifyResult.OK;
    }
}
