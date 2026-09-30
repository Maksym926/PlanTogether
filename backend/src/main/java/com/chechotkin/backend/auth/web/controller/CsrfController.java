package com.chechotkin.backend.auth.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class CsrfController {


    @GetMapping("/csrf")
    public ResponseEntity<Void> getCsrf(CsrfToken token) {
        token.getToken();
        return ResponseEntity.noContent().build();
    }
}
