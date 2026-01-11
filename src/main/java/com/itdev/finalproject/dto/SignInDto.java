package com.itdev.finalproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignInDto(

        @NotNull
        @NotBlank
        String username,

        @NotNull
        @NotBlank
        String password) {

}
