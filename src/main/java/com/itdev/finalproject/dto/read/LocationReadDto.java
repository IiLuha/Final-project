package com.itdev.finalproject.dto.read;

public record LocationReadDto(
        Long id,
        String name,
        String address,
        Integer capacity
) {
}
