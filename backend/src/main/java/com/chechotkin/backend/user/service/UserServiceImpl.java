package com.chechotkin.backend.user.service;

import com.chechotkin.backend.user.model.User;
import com.chechotkin.backend.user.repo.UserRepo;
import com.chechotkin.backend.user.usecase.UserService;

import java.time.Clock;

public class UserServiceImpl implements UserService {
    private final UserRepo users;
    private final Clock clock;

    public UserServiceImpl(UserRepo users, Clock clock){
        this.users = users;
        this.clock =clock;
    }
    public User upsertUser(String email){
        return users.upsertByEmail(email, clock.instant());
    }






}
