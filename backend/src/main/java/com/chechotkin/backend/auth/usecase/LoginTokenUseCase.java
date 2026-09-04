package com.chechotkin.backend.auth.usecase;

import com.chechotkin.backend.auth.service.VerifyResult;

import java.time.Duration;

public interface LoginTokenUseCase {
    String create(String email, String sessionId, String requestIp);

    VerifyResult verify(String email, String code, String sessionId);

    int deleteExpiredOlderThan(Duration retention);
}
