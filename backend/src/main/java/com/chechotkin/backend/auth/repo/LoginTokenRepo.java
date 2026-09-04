package com.chechotkin.backend.auth.repo;

import com.chechotkin.backend.auth.model.LoginToken;

import java.time.Instant;
import java.util.Optional;


public interface LoginTokenRepo {


    void insert(LoginToken token);


    Optional<LoginToken> findActiveByEmail(String email);


    int deleteActiveFor(String email);


    int incrementAttempts(long id);


    boolean consume(long id, Instant at);


    int deleteOlderThan(Instant cutoff);
}
