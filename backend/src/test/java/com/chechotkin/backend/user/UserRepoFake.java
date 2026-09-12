package com.chechotkin.backend.user;

import com.chechotkin.backend.user.model.User;
import com.chechotkin.backend.user.repo.UserRepo;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserRepoFake implements UserRepo {

    Map<String, User> users = new HashMap<>();


    @Override
    public User upsertByEmail(String email, Instant created_at) {
        if(!users.containsKey(email)){
            users.put(email, new User(12L, email, email, created_at));
        }

        return users.get(email);
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return Optional.ofNullable(users.get(email));
    }
}
