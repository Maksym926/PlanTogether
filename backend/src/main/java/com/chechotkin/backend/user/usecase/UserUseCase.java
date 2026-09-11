package com.chechotkin.backend.user.usecase;

import com.chechotkin.backend.user.model.User;

public interface UserUseCase {
    User upsertUser(String email);
}
