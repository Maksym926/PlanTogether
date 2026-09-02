package com.chechotkin.backend.auth.repo;

import com.chechotkin.backend.auth.model.LoginToken;

import java.time.Instant;
import java.util.Optional;

public class LoginTokenRepoImpl implements LoginTokenRepo{
    @Override
    public LoginToken insert(LoginToken token) {
        return null;
    }

    @Override
    public Optional<LoginToken> findActiveByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public int deleteActiveFor(String email) {
        return 0;
    }

    @Override
    public int incrementAttempts(long id) {
        return 0;
    }

    @Override
    public boolean consume(long id, Instant at) {
        return false;
    }

    @Override
    public int deleteOlderThan(Instant cutoff) {
        return 0;
    }
}
