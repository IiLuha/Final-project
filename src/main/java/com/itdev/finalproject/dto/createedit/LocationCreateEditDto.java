package com.itdev.finalproject.dto.createedit;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LocationCreateEditDto(

        @NotNull
        @NotBlank
        String name,

        @NotNull
        @NotBlank
        String address,

        @Min(1)
        Integer capacity
) {
}
