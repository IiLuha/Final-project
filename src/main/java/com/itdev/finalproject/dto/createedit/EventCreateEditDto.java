package com.itdev.finalproject.dto.createedit;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventCreateEditDto(

        @NotNull
        @NotBlank
        String name,

        @NotNull
        @PositiveOrZero
        Integer maxPlaces,

        @NotNull
        @Future
        @DateTimeFormat(pattern = "uuuu-MM-dd'T'HH:mm:ss")
        LocalDateTime date,

        @NotNull
        @Min(30)
        Integer duration,

        @NotNull
        @PositiveOrZero
        BigDecimal cost,

        @NotNull
        @Positive
        Long locationId) {
}
