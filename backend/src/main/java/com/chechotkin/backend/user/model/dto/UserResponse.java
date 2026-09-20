package com.chechotkin.backend.user.model.dto;

import com.chechotkin.backend.user.model.User;

public record UserResponse(
        String email,
        Long id,
        String displayedName
) {
    public static UserResponse mapToUserResponse(User user){
        return  new UserResponse(
                user.email(),
                user.id(),
                user.displayedName()
        );
    }
}
