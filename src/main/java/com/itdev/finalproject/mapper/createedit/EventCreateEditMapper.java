package com.itdev.finalproject.mapper.createedit;

import com.itdev.finalproject.database.entity.EventEntity;
import com.itdev.finalproject.database.repository.LocationRepository;
import com.itdev.finalproject.dto.createedit.EventCreateEditDto;
import com.itdev.finalproject.mapper.Mapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class EventCreateEditMapper implements Mapper<EventCreateEditDto, EventEntity> {

    private final LocationRepository locationRepository;

    public EventCreateEditMapper(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public EventEntity map(EventCreateEditDto fromObject, EventEntity toObject) {
        copy(fromObject, toObject);
        return toObject;
    }

    @Override
    public EventEntity map(EventCreateEditDto fromObject) {
        EventEntity userEntity = new EventEntity();
        copy(fromObject, userEntity);
        return userEntity;
    }

    private void copy(EventCreateEditDto dto, EventEntity userEntity) {
        userEntity.setName(dto.name());
        userEntity.setMaxPlaces(dto.maxPlaces());
        userEntity.setDate(dto.date());
        userEntity.setDuration(dto.duration());
        userEntity.setCost(dto.cost());
        userEntity.setLocation(
                locationRepository.findById(dto.locationId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Could not find location with id=" + dto.locationId() + " for event"))
        );
    }
}
