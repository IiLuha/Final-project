package com.itdev.finalproject.mapper.read;

import com.itdev.finalproject.database.entity.UserEntity;
import com.itdev.finalproject.dto.read.UserReadDto;
import com.itdev.finalproject.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class UserReadMapper implements Mapper<UserEntity, UserReadDto> {

    @Override
    public UserReadDto map(UserEntity object) {
        return new UserReadDto(
                object.getId(),
                object.getUsername(),
                object.getPassword(),
                object.getRole()
        );
    }
}
