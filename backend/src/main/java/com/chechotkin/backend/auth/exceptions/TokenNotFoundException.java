package com.chechotkin.backend.auth.exceptions;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(String email){
        super("Token for this email" + email + "is not found ");
    }
}
