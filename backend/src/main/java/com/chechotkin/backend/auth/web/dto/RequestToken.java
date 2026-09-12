package com.chechotkin.backend.auth.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestToken(
        @NotBlank @Email String email

) {

}
