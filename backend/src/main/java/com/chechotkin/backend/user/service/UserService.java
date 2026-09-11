package com.chechotkin.backend.user.service;

import com.chechotkin.backend.user.model.User;
import com.chechotkin.backend.user.repo.UserRepo;
import com.chechotkin.backend.user.usecase.UserUseCase;

import java.time.Clock;
import java.time.Instant;

public class UserService implements UserUseCase {
    private final UserRepo users;
    private final Clock clock;

    public UserService(UserRepo users, Clock clock){
        this.users = users;
        this.clock =clock;
    }
    public User upsertUser(String email){
        return users.upsertByEmail(email, clock.instant());
    }






}
