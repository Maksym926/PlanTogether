package com.chechotkin.backend.auth.usecase;

import com.chechotkin.backend.errors.Result;
import com.chechotkin.backend.user.model.User;
import com.chechotkin.backend.user.model.dto.UserResponse;

public interface AuthService {
    void request(String email, String sessionId, String ip);

    Result<User> verify(String email, String code, String sessionId);
}
