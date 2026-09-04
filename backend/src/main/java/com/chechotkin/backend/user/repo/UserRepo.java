package com.chechotkin.backend.user.repo;

import com.chechotkin.backend.user.model.User;

import java.time.Instant;
import java.util.Optional;

public interface UserRepo {
    User upsertByEmail(String email, Instant created_at);

    Optional<User> getByEmail(String email);
}
