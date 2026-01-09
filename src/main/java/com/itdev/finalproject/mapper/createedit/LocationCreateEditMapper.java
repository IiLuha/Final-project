package com.itdev.finalproject.mapper.createedit;

import com.itdev.finalproject.database.entity.LocationEntity;
import com.itdev.finalproject.dto.createedit.LocationCreateEditDto;
import com.itdev.finalproject.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class LocationCreateEditMapper implements Mapper<LocationCreateEditDto, LocationEntity> {

    @Override
    public LocationEntity map(LocationCreateEditDto fromObject, LocationEntity toObject) {
        copy(fromObject, toObject);
        return toObject;
    }

    @Override
    public LocationEntity map(LocationCreateEditDto fromObject) {
        LocationEntity locationEntity = new LocationEntity();
        copy(fromObject, locationEntity);
        return locationEntity;
    }

    private void copy(LocationCreateEditDto dto, LocationEntity locationEntity) {
        locationEntity.setName(dto.name());
        locationEntity.setAddress(dto.address());
        locationEntity.setCapacity(dto.capacity());
    }
}
