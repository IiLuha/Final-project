package com.itdev.finalproject.mapper.read;

import com.itdev.finalproject.database.entity.EventEntity;
import com.itdev.finalproject.dto.read.EventReadDto;
import com.itdev.finalproject.mapper.Mapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EventReadMapper implements Mapper<EventEntity, EventReadDto> {

    private final LocationReadMapper locationReadMapper;
    private final UserReadMapper userReadMapper;

    public EventReadMapper(LocationReadMapper locationReadMapper, UserReadMapper userReadMapper) {
        this.locationReadMapper = locationReadMapper;
        this.userReadMapper = userReadMapper;
    }

    @Override
    public EventReadDto map(EventEntity object) {
        return new EventReadDto(
                object.getId(),
                object.getName(),
                userReadMapper.map(object.getOwner()),
                object.getMaxPlaces(),
                object.getOccupiedPlaces(),
                object.getDate(),
                object.getDuration(),
                object.getCost(),
                Optional.ofNullable(object.getLocation())
                        .map(locationReadMapper::map)
                        .orElse(null),
                object.getStatus()
        );
    }
}
