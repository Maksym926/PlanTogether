package com.chechotkin.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {

        Map<String, List<String>> errors = new TreeMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors
                        .computeIfAbsent(error.getField(), field -> new ArrayList<>())
                        .add(error.getDefaultMessage()));

        return ErrorResponse.validation(errors);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadableBody(HttpMessageNotReadableException ex) {
        return ErrorResponse.malformedRequest();
    }
}
