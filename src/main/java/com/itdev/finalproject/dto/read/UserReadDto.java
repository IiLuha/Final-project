package com.itdev.finalproject.dto.read;

import com.itdev.finalproject.database.entity.Role;

public record UserReadDto(
        Long id,
        String username,
        String password,
        Role role
) {
}
