package com.chechotkin.backend.user.model;

import java.time.Instant;

/**
 * Component names are camelCase counterparts of the column names
 * (displayed_name -> displayedName), which is what SimplePropertyRowMapper
 * matches on.
 */
public record User(
        Long id,
        String email,
        String displayedName,
        Instant createdAt
) {
}
