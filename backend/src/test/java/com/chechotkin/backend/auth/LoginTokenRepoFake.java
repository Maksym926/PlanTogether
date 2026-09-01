package com.chechotkin.backend.auth;

import com.chechotkin.backend.auth.model.LoginToken;
import com.chechotkin.backend.auth.repo.LoginTokenRepo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LoginTokenRepoFake implements LoginTokenRepo {
    Map<String, LoginToken> tokens = new HashMap<>();

    public void put(String email, LoginToken token){
        tokens.put(email, token);
    }
    public Optional<LoginToken> get(String email){
        return Optional.ofNullable(tokens.get(email));
    }
}
