package com.chechotkin.backend.auth.helpers;

import java.security.SecureRandom;

public class CodeGeneratorImpl implements CodeGenerator {
    private SecureRandom random = new SecureRandom();
    @Override
    public String generate(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<6; i++){
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }


}
