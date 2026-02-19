package com.itdev.finalproject.dto.kafka;

import com.itdev.finalproject.dto.read.CompactEventReadDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public record EventKafkaMessage(
        Long eventId,
        Long changedBy,
        LocalDateTime changedAt,
        Long ownerId,
        EventModificationType eventModificationType,
        Set<FieldChange<?>> fieldChanges,
        Set<Long> attendeeIds
) {

    public EventKafkaMessage(Long eventId, Long changedBy, LocalDateTime changedAt, Long ownerId, EventModificationType eventModificationType) {
        this(eventId, changedBy, changedAt, ownerId, eventModificationType, new HashSet<>(), new HashSet<>());
    }

    public void addAttendeeIds(Collection<? extends Long> ids) {
        attendeeIds.addAll(ids);
    }

    public void addFieldChange(FieldChange<?> fieldChange) {
        fieldChanges.add(fieldChange);
    }

    public void extractFieldChanges(CompactEventReadDto oldDto, CompactEventReadDto newDto) {
        if (newDto == null) return;

        FieldChange<String> name = new FieldChange<>(
                oldDto == null ? null : oldDto.name(),
                newDto.name(),
                "name",
                String.class
        );
        if (!name.newValue().equals(name.oldValue())) this.addFieldChange(name);

        FieldChange<Integer> maxPlaces = new FieldChange<>(
                oldDto == null ? null : oldDto.maxPlaces(),
                newDto.maxPlaces(),
                "maxPlaces",
                Integer.class
        );
        if (!maxPlaces.newValue().equals(maxPlaces.oldValue())) this.addFieldChange(maxPlaces);

        FieldChange<LocalDateTime> date = new FieldChange<>(
                oldDto == null ? null : oldDto.date(),
                newDto.date(),
                "date",
                LocalDateTime.class
        );
        if (!date.newValue().equals(date.oldValue())) this.addFieldChange(date);

        FieldChange<BigDecimal> cost = new FieldChange<>(
                oldDto == null ? null : oldDto.cost(),
                newDto.cost(),
                "cost",
                BigDecimal.class
        );
        if (cost.oldValue() == null || cost.newValue().compareTo(cost.oldValue()) != 0) this.addFieldChange(cost);

        FieldChange<Integer> duration = new FieldChange<>(
                oldDto == null ? null : oldDto.duration(),
                newDto.duration(),
                "duration",
                Integer.class
        );
        if (!duration.newValue().equals(duration.oldValue())) this.addFieldChange(duration);

        FieldChange<Long> locationId = new FieldChange<>(
                oldDto == null ? null : oldDto.locationId(),
                newDto.locationId(),
                "locationId",
                Long.class
        );
        if (!locationId.newValue().equals(locationId.oldValue())) this.addFieldChange(locationId);

        FieldChange<String> status = new FieldChange<>(
                oldDto == null ? null : oldDto.status().name(),
                newDto.status().name(),
                "status",
                String.class
        );
        if (!status.newValue().equals(status.oldValue())) this.addFieldChange(status);
    }
}
