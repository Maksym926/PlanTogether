package com.chechotkin.backend.auth;

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
