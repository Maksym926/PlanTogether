package com.chechotkin.backend.auth.service;

import com.chechotkin.backend.auth.usecase.LoginTokenUseCase;

public class AuthService {

    private final LoginTokenUseCase loginTokenUseCase;

    public AuthService(LoginTokenUseCase loginTokenUseCase){
        this.loginTokenUseCase = loginTokenUseCase;
    }





}
