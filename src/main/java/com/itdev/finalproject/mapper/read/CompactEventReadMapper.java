package com.itdev.finalproject.mapper.read;

import com.itdev.finalproject.database.entity.EventEntity;
import com.itdev.finalproject.dto.read.CompactEventReadDto;
import com.itdev.finalproject.dto.read.EventReadDto;
import com.itdev.finalproject.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class CompactEventReadMapper implements Mapper<EventEntity, CompactEventReadDto> {

    @Override
    public CompactEventReadDto map(EventEntity entity) {
        return new CompactEventReadDto(
                entity.getId(),
                entity.getName(),
                entity.getOwner().getId(),
                entity.getMaxPlaces(),
                entity.getOccupiedPlaces(),
                entity.getDate(),
                entity.getDuration(),
                entity.getCost(),
                entity.getLocation().getId(),
                entity.getStatus()
        );
    }

    public CompactEventReadDto map(EventReadDto dto) {
        return new CompactEventReadDto(
                dto.id(),
                dto.name(),
                dto.owner().id(),
                dto.maxPlaces(),
                dto.occupiedPlaces(),
                dto.date(),
                dto.duration(),
                dto.cost(),
                dto.location().id(),
                dto.status()
        );
    }
}
