package com.chechotkin.backend.auth;

import com.chechotkin.backend.auth.helpers.CodeGenerator;

public class FixedCodeGenerator implements CodeGenerator {
    @Override
    public String generate() {
        return "123456";
    }
}
