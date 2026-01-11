package com.itdev.finalproject.dto.createedit;

import com.itdev.finalproject.validation.group.CreateAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateEditDto(

        @NotNull
        @NotBlank
        String username,

        @NotNull(groups = {CreateAction.class})
        @NotBlank
        String password) {

}
