package com.chechotkin.backend.auth.web.controller;

import com.chechotkin.backend.auth.usecase.AuthService;
import com.chechotkin.backend.auth.web.dto.RequestToken;
import com.chechotkin.backend.auth.web.dto.VerifyToken;
import com.chechotkin.backend.errors.Result;
import com.chechotkin.backend.security.AuthPrincipal;
import com.chechotkin.backend.security.SessionAuthenticator;
import com.chechotkin.backend.user.model.User;
import com.chechotkin.backend.user.model.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthService authService;
    private final SessionAuthenticator sessionAuthenticator;

    public AuthController(AuthService authService, SessionAuthenticator sessionAuthenticator){
        this.authService = authService;
        this.sessionAuthenticator = sessionAuthenticator;
    }

    @PostMapping("/request")
    public ResponseEntity<Void> makeRequest(@Valid @RequestBody RequestToken body, HttpServletRequest request){
        HttpSession session = request.getSession(true);
        authService.request(body.email(), session.getId(), request.getRemoteAddr());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyToken body, HttpServletRequest request,
                                    HttpServletResponse response){
        HttpSession session = request.getSession(true);
        Result<User> result = authService.verify(body.email(), body.code(), session.getId());
        if (result.isSuccess()) {
            User user = result.getResult();
            sessionAuthenticator.login(new AuthPrincipal(user.id(), user.email()), request, response);
            return ResponseEntity.ok(UserResponse.mapToUserResponse(user));
        }
        return ResponseEntity.badRequest().body(result.getError());
    }
}
