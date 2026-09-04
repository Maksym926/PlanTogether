package com.chechotkin.backend.user.model;

import java.time.Instant;

public record User (
    Long id,
    String email,
    String displayed_name,
    Instant created_at
){

}
