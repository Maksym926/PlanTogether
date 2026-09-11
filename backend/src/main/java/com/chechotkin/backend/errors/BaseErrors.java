package com.chechotkin.backend.errors;

import lombok.Getter;

@Getter
public class BaseErrors {
    private final String message;
    private final String code;



    protected BaseErrors(String message, String code){
        this.message = message;
        this.code = code;
    }
}
