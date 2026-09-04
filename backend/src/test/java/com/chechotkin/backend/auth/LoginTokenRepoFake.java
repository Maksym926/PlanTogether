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
    public void insert(LoginToken token) {
        long id = nextId++;
        rows.put(id, token.withId(id));
    }

    @Override
    public Optional<LoginToken> findActiveByEmail(String email) {

        return rows.values().stream()
                .filter(t -> email.equals(t.email()) && t.consumedAt() == null)
                .reduce((first, second) -> second);
    }

    @Override
    public int deleteActiveFor(String email) {
        int before = rows.size();
        rows.values().removeIf(t -> email.equals(t.email()) && t.consumedAt() == null);
        return before - rows.size();
    }

    @Override
    public int incrementAttempts(long id) {
        LoginToken token = rows.get(id);
        if (token == null) {
            return 0;
        }
        LoginToken updated = token.withAttempts(token.attempts() + 1);
        rows.put(id, updated);
        return updated.attempts();
    }

    @Override
    public boolean consume(long id, Instant at) {
        LoginToken token = rows.get(id);
        if (token == null || token.consumedAt() != null) {
            return false;
        }
        rows.put(id, token.withConsumedAt(at));
        return true;
    }

    @Override
    public int deleteOlderThan(Instant cutoff) {
        int before = rows.size();
        rows.values().removeIf(t -> t.createdAt().isBefore(cutoff));
        return before - rows.size();
    }
}
