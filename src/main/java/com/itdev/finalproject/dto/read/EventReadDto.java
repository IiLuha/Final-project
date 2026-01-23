package com.itdev.finalproject.dto.read;

import com.itdev.finalproject.database.entity.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventReadDto(

        Long id,
        String name,
        UserReadDto owner,
        Integer maxPlaces,
        Integer occupiedPlaces,
        LocalDateTime date,
        Integer duration,
        BigDecimal cost,
        LocationReadDto location,
        EventStatus status
) {
}
