package com.chechotkin.backend.user.usecase;

import com.chechotkin.backend.user.model.User;


import java.util.Optional;

public interface UserService {
    User upsertUser(String email);

    Optional<User> findByEmail(String email);
}
