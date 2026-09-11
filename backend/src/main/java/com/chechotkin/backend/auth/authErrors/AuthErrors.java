package com.chechotkin.backend.auth.authErrors;

import com.chechotkin.backend.errors.BaseErrors;

public class AuthErrors extends BaseErrors {

    protected AuthErrors(String message, String code) {
        super(message, code);
    }

    public static AuthErrors wrongCode() {
        return  new AuthErrors("Wrong code", "wrong_code");
    }

    public static AuthErrors expired() {
        return  new AuthErrors("Code expired", "expired_code");
    }

    public static AuthErrors tooManyAttempts() {
        return new AuthErrors("Too many attempts", "too_many_attempts");
    }

    public static AuthErrors wrongSession() {
        return  new AuthErrors("Wrong session", "wrong_session");
    }
}
