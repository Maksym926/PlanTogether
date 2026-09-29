package com.chechotkin.backend.auth.service;

import com.chechotkin.backend.auth.authErrors.AuthErrors;
import com.chechotkin.backend.auth.usecase.AuthService;
import com.chechotkin.backend.auth.usecase.LoginTokenService;
import com.chechotkin.backend.errors.Result;
import com.chechotkin.backend.user.model.User;
import com.chechotkin.backend.user.usecase.UserService;

import java.util.Locale;

public class AuthServiceImpl implements AuthService {

    private final LoginTokenService loginTokenService;
    private final UserService userService;

    public AuthServiceImpl(LoginTokenService loginTokenService, UserService userService){
        this.loginTokenService = loginTokenService;
        this.userService = userService;
    }
    @Override
    public void request(String email, String sessionId, String ip){
        loginTokenService.create(normalize(email), sessionId, ip);
    }
    @Override
    public Result<User> verify(String email, String code, String sessionId){
        String normalizedEmail = normalize(email);
        VerifyResult result = loginTokenService.verify(normalizedEmail, code, sessionId);
        if(result == VerifyResult.OK){
            User user = userService.upsertUser(normalizedEmail);
            return  Result.success(user);
        }
        return switch (result) {
            case VerifyResult.WRONG_CODE -> Result.error(AuthErrors.wrongCode());
            case VerifyResult.EXPIRED -> Result.error(AuthErrors.expired());
            case VerifyResult.TOO_MANY_ATTEMPTS -> Result.error(AuthErrors.tooManyAttempts());
            case VerifyResult.WRONG_SESSION -> Result.error(AuthErrors.wrongSession());
            case VerifyResult.CONSUMED -> Result.error(AuthErrors.wrongCode());
            case VerifyResult.OK -> throw new IllegalStateException("handled above");
        };
    }


    private static String normalize(String email){
        return email.trim().toLowerCase(Locale.ROOT);
    }





}
