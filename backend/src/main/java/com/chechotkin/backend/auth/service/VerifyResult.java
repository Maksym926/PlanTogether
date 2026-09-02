package com.chechotkin.backend.auth.service;

public enum VerifyResult {
    OK,
    CONSUMED,
    TOO_MANY_ATTEMPTS,
    EXPIRED,
    WRONG_SESSION,
    WRONG_CODE
}
