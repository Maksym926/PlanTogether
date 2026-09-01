package com.chechotkin.backend.auth.service;

import com.chechotkin.backend.auth.CodeGenerator;
import com.chechotkin.backend.auth.SHA1Hash;
import com.chechotkin.backend.auth.exceptions.TokenNotFoundException;
import com.chechotkin.backend.auth.model.LoginToken;
import com.chechotkin.backend.auth.repo.LoginTokenRepo;

import java.time.LocalDateTime;

public class LoginTokenService {

    private final CodeGenerator generator;
    private final LoginTokenRepo loginTokenRepo;

    public LoginTokenService(CodeGenerator generator, LoginTokenRepo loginTokenRepo) {
        this.generator = generator;
        this.loginTokenRepo = loginTokenRepo;
    }

    public void create(LoginToken token) {
        loginTokenRepo.put(token.getEmail(), token);
    }

    public boolean verify(String email, String code) {
        String hashedCode = SHA1Hash.hashString(code);
        LoginToken token = loginTokenRepo.get(email).orElseThrow(() -> new TokenNotFoundException(email));
        if(hashedCode.equals(token.getToken_hash())){
            if(token.getConsumedAt() == null){
                token.setConsumedAt(LocalDateTime.now());
                return true;
            }
        }
        return false;
    }
}
