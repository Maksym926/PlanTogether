package com.chechotkin.backend.auth;

import com.chechotkin.backend.auth.helpers.CodeGenerator;

public class FixedCodeGenerator implements CodeGenerator {
    private String code;
    @Override
    public String generate() {
        return code;
    }
    public FixedCodeGenerator(String code){
        this.code = code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
