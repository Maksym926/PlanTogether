package com.chechotkin.backend.user.controller;

import com.chechotkin.backend.security.AuthPrincipal;
import com.chechotkin.backend.user.model.User;
import com.chechotkin.backend.user.model.dto.UserResponse;
import com.chechotkin.backend.user.usecase.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getAuthPrincipal(@AuthenticationPrincipal AuthPrincipal principal){
        Optional<User> user = userService.findByEmail(principal.email());
        if(user.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(UserResponse.mapToUserResponse(user.get()));
    }
}
