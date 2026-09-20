package com.chechotkin.backend.security;

import java.io.Serializable;


public record AuthPrincipal(
        Long id,
        String email
) implements Serializable {
}
