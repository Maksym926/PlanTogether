package com.chechotkin.backend.auth;

import com.chechotkin.backend.auth.exceptions.FailedToCreateCodeException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class CodeHasher {

    private static final String SEPARATOR = ":";

    public static String hash(String code, String email) {
        MessageDigest md;
        try{
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new FailedToCreateCodeException();
        }

        String input = code + SEPARATOR + normalizeEmail(email);
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
