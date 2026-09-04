package com.chechotkin.backend.user.service;

import com.chechotkin.backend.user.repo.UserRepo;

public class UserService {
    private final UserRepo users;

    public UserService(UserRepo users){
        this.users = users;
    }


}
