package com.itdev.finalproject.dto.filter;

import com.itdev.finalproject.database.entity.EventStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventFilter(

        String name,
        Integer maxPlacesMin,
        Integer maxPlacesMax,
        @DateTimeFormat(pattern = "uuuu-MM-dd'T'HH:mm:ss")
        LocalDateTime before,
        @DateTimeFormat(pattern = "uuuu-MM-dd'T'HH:mm:ss")
        LocalDateTime after,
        BigDecimal costMin,
        BigDecimal costMax,
        Integer durationMin,
        Integer durationMax,
        Long locationId,
        EventStatus[] eventStatuses
) {
}
