package com.itdev.finalproject.dto.read;

import com.itdev.finalproject.database.entity.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompactEventReadDto(

        Long id,
        String name,
        Long ownerId,
        Integer maxPlaces,
        Integer occupiedPlaces,
        LocalDateTime date,
        Integer duration,
        BigDecimal cost,
        Long locationId,
        EventStatus status
) {
    public CompactEventReadDto changeStatus(EventStatus status) {
        return new CompactEventReadDto(
                this.id(),
                this.name(),
                this.ownerId(),
                this.maxPlaces(),
                this.occupiedPlaces(),
                this.date(),
                this.duration(),
                this.cost(),
                this.locationId(),
                status
        );
    }
}
