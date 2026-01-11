package com.itdev.finalproject.mapper.createedit;

import com.itdev.finalproject.database.entity.UserEntity;
import com.itdev.finalproject.dto.createedit.UserCreateEditDto;
import com.itdev.finalproject.mapper.Mapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
public class UserCreateEditMapper implements Mapper<UserCreateEditDto, UserEntity> {

    private final PasswordEncoder passwordEncoder;

    public UserCreateEditMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserEntity map(UserCreateEditDto fromObject, UserEntity toObject) {
        copy(fromObject, toObject);
        return toObject;
    }

    @Override
    public UserEntity map(UserCreateEditDto fromObject) {
        UserEntity userEntity = new UserEntity();
        copy(fromObject, userEntity);
        return userEntity;
    }

    private void copy(UserCreateEditDto dto, UserEntity userEntity) {
        userEntity.setUsername(dto.username());

        Optional.ofNullable(dto.password())
                .filter(StringUtils::hasText)
                .map(passwordEncoder::encode)
                .ifPresent(userEntity::setPassword);
    }
}
