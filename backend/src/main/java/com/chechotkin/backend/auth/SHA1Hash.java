package com.chechotkin.backend.auth;

import com.chechotkin.backend.auth.exceptions.FailedToCreateCodeException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Hash {
    public static String hashString(String code)  {
        MessageDigest md;
        try{
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new FailedToCreateCodeException();
        }

        byte[] hash = md.digest(code.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        String hashedCode = hex.toString();

        return hashedCode;

    }
}
