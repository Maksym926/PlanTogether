package com.chechotkin.backend.auth.service;

import com.chechotkin.backend.auth.helpers.CodeGenerator;
import com.chechotkin.backend.auth.helpers.CodeHasher;
import com.chechotkin.backend.auth.repo.LoginTokenRepo;
import com.chechotkin.backend.auth.usecase.LoginTokenService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class LoginTokenServiceServiceImpl implements LoginTokenService {

    private static final Duration TIME_TO_LIVE = Duration.ofMinutes(15);
    private static final int MAX_ATTEMPTS = 3;

    private final CodeGenerator generator;
    private final LoginTokenRepo loginTokenRepo;
    private final Clock clock;

    public LoginTokenServiceServiceImpl(CodeGenerator generator, LoginTokenRepo loginTokenRepo, Clock clock) {
        this.generator = generator;
        this.loginTokenRepo = loginTokenRepo;
        this.clock = clock;
    }


    @Override
    public String create(String email, String sessionId, String requestIp) {
        loginTokenRepo.deleteActiveFor(email);

        String code = generator.generate();
        Instant now = clock.instant();

        loginTokenRepo.insert(com.chechotkin.backend.auth.model.LoginToken.issue(
                CodeHasher.hash(code, email),
                email,
                sessionId,
                requestIp,
                now,
                now.plus(TIME_TO_LIVE)));

        return code;
    }

    @Override
    public VerifyResult verify(String email, String code, String sessionId) {
        Optional<com.chechotkin.backend.auth.model.LoginToken> active = loginTokenRepo.findActiveByEmail(email);
        if (active.isEmpty()) {

            return VerifyResult.WRONG_CODE;
        }

        com.chechotkin.backend.auth.model.LoginToken token = active.get();
        Instant now = clock.instant();


        if (token.attempts() >= MAX_ATTEMPTS) {
            return VerifyResult.TOO_MANY_ATTEMPTS;
        }
        if (now.isAfter(token.expiresAt())) {
            return VerifyResult.EXPIRED;
        }
        if (!sessionId.equals(token.sessionId())) {
            return VerifyResult.WRONG_SESSION;
        }

        if (!CodeHasher.hash(code, email).equals(token.tokenHash())) {
            loginTokenRepo.incrementAttempts(token.id());
            return VerifyResult.WRONG_CODE;
        }


        return loginTokenRepo.consume(token.id(), now)
                ? VerifyResult.OK
                : VerifyResult.CONSUMED;
    }


    @Override
    public int deleteExpiredOlderThan(Duration retention) {
        return loginTokenRepo.deleteOlderThan(clock.instant().minus(retention));
    }
}
