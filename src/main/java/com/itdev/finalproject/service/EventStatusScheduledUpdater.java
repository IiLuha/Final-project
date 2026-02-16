package com.itdev.finalproject.service;

import com.itdev.finalproject.database.entity.EventStatus;
import com.itdev.finalproject.database.repository.EventRepository;
import com.itdev.finalproject.dto.read.CompactEventReadDto;
import com.itdev.finalproject.dto.kafka.EventKafkaMessage;
import com.itdev.finalproject.dto.kafka.FieldChange;
import com.itdev.finalproject.dto.kafka.EventModificationType;
import com.itdev.finalproject.mapper.read.CompactEventReadMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

@Service
@Transactional
public class EventStatusScheduledUpdater {

    private final EventRepository eventRepository;
    private final CompactEventReadMapper compactEventReadMapper;
    private final EventKafkaMessageSender eventKafkaMessageSender;

    public EventStatusScheduledUpdater(EventRepository eventRepository,
                                       CompactEventReadMapper compactEventReadMapper,
                                       EventKafkaMessageSender eventKafkaMessageSender) {
        this.eventRepository = eventRepository;
        this.compactEventReadMapper = compactEventReadMapper;
        this.eventKafkaMessageSender = eventKafkaMessageSender;
    }

    @Scheduled(cron = "${event.stats.cron}")
    public void updateStartedEventStatuses() {
        updateEventStatuses(List.of(EventStatus.WAIT_START), EventStatus.STARTED, this::notFinished);
    }

    @Scheduled(cron = "${event.stats.cron}")
    public void updateFinishedEventStatuses() {
        updateEventStatuses(List.of(EventStatus.STARTED, EventStatus.WAIT_START),
                EventStatus.FINISHED, this::isFinished);
    }

    public void updateEventStatuses(List<EventStatus> statusesFrom, EventStatus statusTo,
                                    Predicate<? super CompactEventReadDto> filter) {
        List<CompactEventReadDto> events = eventRepository
                .findAllByDateBeforeAndStatusIn(LocalDateTime.now(), statusesFrom).stream()
                .map(compactEventReadMapper::map)
                .filter(filter)
                .map(compactEventReadDto -> compactEventReadDto.changeStatus(statusTo))
                .toList();
        Long[] ids = events.stream()
                .mapToLong(CompactEventReadDto::id)
                .boxed().toArray(Long[]::new);
        if (ids.length > 0) eventRepository.updateStatusesByIds(statusTo.name(), ids);

        for (CompactEventReadDto oldDto : events) {
            EventKafkaMessage eventKafkaMessage = new EventKafkaMessage(oldDto.id(),
                    null,
                    LocalDateTime.now(),
                    oldDto.ownerId(),
                    EventModificationType.UPDATED);
            FieldChange<EventStatus> status = new FieldChange<>(
                    oldDto.status(),
                    statusTo,
                    "status",
                    EventStatus.class
            );
            if (status.newValue().equals(status.oldValue())) eventKafkaMessage.addFieldChange(status);
            eventKafkaMessageSender.send(eventKafkaMessage);
        }
    }

    private boolean notFinished(CompactEventReadDto event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = event.date().plusMinutes(event.duration());
        return now.isBefore(threshold);
    }

    private boolean isFinished(CompactEventReadDto event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = event.date().plusMinutes(event.duration());
        return now.isAfter(threshold);
    }
}
