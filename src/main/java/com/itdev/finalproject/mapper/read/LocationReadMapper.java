package com.itdev.finalproject.mapper.read;

import com.itdev.finalproject.database.entity.LocationEntity;
import com.itdev.finalproject.dto.read.LocationReadDto;
import com.itdev.finalproject.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class LocationReadMapper implements Mapper<LocationEntity, LocationReadDto> {

    @Override
    public LocationReadDto map(LocationEntity object) {
        return new LocationReadDto(
                object.getId(),
                object.getName(),
                object.getAddress(),
                object.getCapacity()
        );
    }
}
