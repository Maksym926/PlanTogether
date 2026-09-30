package com.chechotkin.backend.auth;

import com.chechotkin.backend.auth.notifier.CodeNotifierFake;
import com.chechotkin.backend.auth.notifier.CodeNotifierFake.Sent;
import com.chechotkin.backend.auth.service.AuthServiceImpl;
import com.chechotkin.backend.auth.service.LoginTokenServiceServiceImpl;
import com.chechotkin.backend.auth.usecase.AuthService;
import com.chechotkin.backend.auth.usecase.LoginTokenService;
import com.chechotkin.backend.user.UserRepoFake;
import com.chechotkin.backend.user.service.UserServiceImpl;
import com.chechotkin.backend.user.usecase.UserService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


public class AuthServiceTests {

    private static final String EMAIL = "max@gmail.com";
    private static final String CODE = "123456";
    private static final String SESSION_ID = "session-1";
    private static final String IP = "127.0.0.1";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC);
    private final LoginTokenRepoFake loginTokens = new LoginTokenRepoFake();
    private final UserRepoFake users = new UserRepoFake();
    private final CodeNotifierFake notifier = new CodeNotifierFake();

    private final LoginTokenService loginTokenService =
            new LoginTokenServiceServiceImpl(new FixedCodeGenerator(CODE), loginTokens, clock);
    private final UserService userService = new UserServiceImpl(users, clock);
    private final AuthService authService = new AuthServiceImpl(loginTokenService, userService, notifier);

    @Test
    void requestingACode_sendsItToTheAddressThatAsked() {
        authService.request(EMAIL, SESSION_ID, IP);

        assertThat(notifier.lastSent()).isEqualTo(new Sent(EMAIL, CODE));
    }

    @Test
    void requestingWithAMixedCaseAddress_sendsToTheNormalisedOne() {
        authService.request(" Max@Gmail.COM ", SESSION_ID, IP);

        assertThat(notifier.lastSent().email()).isEqualTo(EMAIL);
        assertThat(loginTokens.findActiveByEmail(EMAIL)).isPresent();
    }

    @Test
    void aFailedDelivery_doesNotFailTheRequest() {
        notifier.failWith(new RuntimeException("smtp down"));

        assertThatCode(() -> authService.request(EMAIL, SESSION_ID, IP))
                .doesNotThrowAnyException();

        assertThat(loginTokens.findActiveByEmail(EMAIL)).isPresent();
    }

    @Test
    void theEmailedCode_verifies() {
        authService.request(EMAIL, SESSION_ID, IP);

        var result = authService.verify(EMAIL, notifier.lastSent().code(), SESSION_ID);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult().email()).isEqualTo(EMAIL);
    }


}
