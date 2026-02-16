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

    private void copy(EventCreateEditDto dto, EventEntity eventEntity) {
        eventEntity.setName(dto.name());
        eventEntity.setMaxPlaces(dto.maxPlaces());
        eventEntity.setOccupiedPlaces(dto.occupiedPlaces());
        eventEntity.setDate(dto.date());
        eventEntity.setDuration(dto.duration());
        eventEntity.setCost(dto.cost());
        eventEntity.setLocation(
                locationRepository.findById(dto.locationId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Could not find location with id=" + dto.locationId() + " for event"))
        );
    }
}
