package com.chechotkin.backend.auth;

import com.chechotkin.backend.auth.exceptions.FailedToCreateCodeException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CodeGenerator {
    private SecureRandom random = new SecureRandom();
    public String generate(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<6; i++){
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }


}
