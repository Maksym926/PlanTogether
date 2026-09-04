package com.chechotkin.backend.errors;

import lombok.Getter;

@Getter
public class BaseError {
    private final String message;
    private final String code;



    protected BaseError(String message, String code){
        this.message = message;
        this.code = code;
    }
}
