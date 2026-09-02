package com.chechotkin.backend.auth;

import com.chechotkin.backend.auth.model.LoginToken;
import com.chechotkin.backend.auth.repo.LoginTokenRepo;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;


public class LoginTokenRepoFake implements LoginTokenRepo {

    private final Map<Long, LoginToken> rows = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public LoginToken insert(LoginToken token) {
        token.setId(nextId++);
        rows.put(token.getId(), token);
        return token;
    }

    @Override
    public Optional<LoginToken> findActiveByEmail(String email) {
        return rows.values().stream()
                .filter(t -> email.equals(t.getEmail()) && t.getConsumedAt() == null)
                .reduce((first, second) -> second);
    }

    @Override
    public int deleteActiveFor(String email) {
        int before = rows.size();
        rows.values().removeIf(t -> email.equals(t.getEmail()) && t.getConsumedAt() == null);
        return before - rows.size();
    }

    @Override
    public int incrementAttempts(long id) {
        LoginToken token = rows.get(id);
        if (token == null) {
            return 0;
        }
        token.setAttempts(token.getAttempts() + 1);
        return token.getAttempts();
    }

    @Override
    public boolean consume(long id, Instant at) {
        LoginToken token = rows.get(id);
        if (token == null || token.getConsumedAt() != null) {
            return false;
        }
        token.setConsumedAt(at);
        return true;
    }

    @Override
    public int deleteOlderThan(Instant cutoff) {
        int before = rows.size();
        rows.values().removeIf(t -> t.getCreatedAt().isBefore(cutoff));
        return before - rows.size();
    }
}
