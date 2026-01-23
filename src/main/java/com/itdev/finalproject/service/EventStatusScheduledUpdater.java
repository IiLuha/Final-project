package com.itdev.finalproject.service;

import com.itdev.finalproject.database.entity.EventEntity;
import com.itdev.finalproject.database.entity.EventStatus;
import com.itdev.finalproject.database.repository.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional()
public class EventStatusScheduledUpdater {

    private final EventRepository eventRepository;

    public EventStatusScheduledUpdater(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(cron = "${event.stats.cron}")
    public void updateEventStatuses() {
        List<EventEntity> startedEvents = eventRepository
                .findAllByDateBeforeAndStatusIs(LocalDateTime.now(), EventStatus.WAIT_START);
        Long[] ids = startedEvents.stream()
                .filter(this::notFinished)
                .mapToLong(EventEntity::getId)
                .boxed().toArray(Long[]::new);
        if (ids.length > 0) eventRepository.updateStatusesByIds(EventStatus.STARTED.name(), ids);

        List<EventEntity> finishedEvents = eventRepository
                .findAllByDateBeforeAndStatusIn(LocalDateTime.now(),
                        List.of(EventStatus.STARTED, EventStatus.WAIT_START));
        ids = finishedEvents.stream()
                .filter(this::isFinished)
                .mapToLong(EventEntity::getId)
                .boxed().toArray(Long[]::new);
        if (ids.length > 0) eventRepository.updateStatusesByIds(EventStatus.FINISHED.name(), ids);
    }

    private boolean notFinished(EventEntity event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = event.getDate().plusMinutes(event.getDuration());
        return now.isBefore(threshold);
    }

    private boolean isFinished(EventEntity event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = event.getDate().plusMinutes(event.getDuration());
        return now.isAfter(threshold);
    }
}
