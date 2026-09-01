package com.chechotkin.backend.auth.repo;

import com.chechotkin.backend.auth.model.LoginToken;

import java.util.Optional;

public interface LoginTokenRepo {
    void put(String id, LoginToken token);
    Optional<LoginToken> get(String id);

}
