package com.chechotkin.backend.auth.service;

import com.chechotkin.backend.auth.authErrors.AuthErrors;
import com.chechotkin.backend.auth.usecase.LoginTokenUseCase;
import com.chechotkin.backend.errors.Result;
import com.chechotkin.backend.user.model.User;
import com.chechotkin.backend.user.usecase.UserUseCase;

public class AuthService {

    private final LoginTokenUseCase loginTokenUseCase;
    private final UserUseCase userUseCase;

    public AuthService(LoginTokenUseCase loginTokenUseCase, UserUseCase userUseCase){
        this.loginTokenUseCase = loginTokenUseCase;
        this.userUseCase = userUseCase;
    }
    public void request(String email, String sessionId, String ip){
        loginTokenUseCase.create(email,sessionId, ip);
    }
    public Result<User> verify(String email, String code, String sessionId){
        VerifyResult result = loginTokenUseCase.verify(email, code, sessionId);
        if(result == VerifyResult.OK){
            User user = userUseCase.upsertUser(email);
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





}
