package com.itdev.finalproject.service;

import com.itdev.finalproject.database.entity.EventEntity;
import com.itdev.finalproject.database.entity.EventStatus;
import com.itdev.finalproject.database.entity.Role;
import com.itdev.finalproject.database.entity.UserEntity;
import com.itdev.finalproject.database.predicate.EventJpaPredicateBuilder;
import com.itdev.finalproject.database.repository.EventRepository;
import com.itdev.finalproject.database.repository.UserRepository;
import com.itdev.finalproject.dto.AuthenticatedUser;
import com.itdev.finalproject.dto.createedit.EventCreateEditDto;
import com.itdev.finalproject.dto.filter.EventFilter;
import com.itdev.finalproject.dto.read.CompactEventReadDto;
import com.itdev.finalproject.dto.kafka.EventKafkaMessage;
import com.itdev.finalproject.dto.kafka.EventModificationType;
import com.itdev.finalproject.dto.read.EventReadDto;
import com.itdev.finalproject.dto.read.LocationReadDto;
import com.itdev.finalproject.dto.read.UserReadDto;
import com.itdev.finalproject.mapper.read.UserReadMapper;
import com.itdev.finalproject.mapper.read.CompactEventReadMapper;
import com.itdev.finalproject.mapper.createedit.EventCreateEditMapper;
import com.itdev.finalproject.mapper.read.EventReadMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EventService {

    private final LocationService locationService;
    private final EventRepository eventRepository;
    private final EventCreateEditMapper eventCreateEditMapper;
    private final EventReadMapper eventReadMapper;
    private final CompactEventReadMapper compactEventReadMapper;
    private final EventJpaPredicateBuilder eventJpaPredicateBuilder;
    private final UserRepository userRepository;
    private final EventKafkaMessageSender eventKafkaMessageSender;
    private final UserReadMapper userReadMapper;

    public EventService(LocationService locationService,
                        EventRepository eventRepository,
                        EventCreateEditMapper eventCreateEditMapper,
                        EventReadMapper eventReadMapper,
                        CompactEventReadMapper compactEventReadMapper,
                        EventJpaPredicateBuilder eventJpaPredicateBuilder,
                        UserRepository userRepository,
                        EventKafkaMessageSender eventKafkaMessageSender, UserReadMapper userReadMapper) {
        this.locationService = locationService;
        this.eventRepository = eventRepository;
        this.eventCreateEditMapper = eventCreateEditMapper;
        this.eventReadMapper = eventReadMapper;
        this.compactEventReadMapper = compactEventReadMapper;
        this.eventJpaPredicateBuilder = eventJpaPredicateBuilder;
        this.userRepository = userRepository;
        this.eventKafkaMessageSender = eventKafkaMessageSender;
        this.userReadMapper = userReadMapper;
    }

    public Page<EventReadDto> findAll(EventFilter filter, Pageable pageable) {
        Specification<EventEntity> specification = eventJpaPredicateBuilder.createSpecification(filter);
        return eventRepository.findAll(specification, pageable)
                .map(eventReadMapper::map);
    }

    public Optional<EventReadDto> findById(Long id) {
        return eventRepository.findById(id)
                .map(eventReadMapper::map);
    }

    public CompactEventReadDto create(EventCreateEditDto createEditDto, AuthenticatedUser authenticatedUser) {
        UserEntity owner = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Authenticated user with id=%s not found".formatted(authenticatedUser.getId())));
        LocationReadDto location = locationService.findById(createEditDto.locationId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Location with id=%s not found".formatted(createEditDto.locationId())));

        if (location.capacity() < createEditDto.maxPlaces()) throw new IllegalArgumentException(
                "It is not allowed to set the maxPlaces greater than " +
                        "the location capacity (%s)".formatted(location.capacity())
        );

        CompactEventReadDto compactEventReadDto = Optional.of(createEditDto)
                .map(eventCreateEditMapper::map).stream()
                .peek(event -> event.setStatus(EventStatus.WAIT_START))
                .peek(event -> event.setOwner(owner)).findFirst()
                .map(eventRepository::save)
                .map(compactEventReadMapper::map)
                .orElseThrow();

        EventKafkaMessage eventKafkaMessage = new EventKafkaMessage(compactEventReadDto.id(),
                authenticatedUser.getId(),
                LocalDateTime.now(),
                compactEventReadDto.ownerId(),
                EventModificationType.CREATED);
        eventKafkaMessage.extractFieldChanges(null, compactEventReadDto);
        eventKafkaMessageSender.send(eventKafkaMessage);

        return compactEventReadDto;
    }

    public CompactEventReadDto update(Long id, EventCreateEditDto createEditDto, AuthenticatedUser authenticatedUser) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean notAdminOrOwner = !(authenticatedUser.getAuthorities().contains(Role.ADMIN) ||
                event.getOwner().getId().equals(authenticatedUser.getId()));
        if (notAdminOrOwner) throw new AuthorizationDeniedException("Access Denied");

        CompactEventReadDto toUpdate = compactEventReadMapper.map(event);

        LocationReadDto location = locationService.findById(createEditDto.locationId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Location with id=%s not found".formatted(createEditDto.locationId())));
        if (createEditDto.maxPlaces() < event.getOccupiedPlaces()) throw new IllegalArgumentException(
                "It is not allowed to set the maxPlaces less than " +
                        "the number of already registered users (%s)".formatted(event.getOccupiedPlaces())
        );
        if (location.capacity() < createEditDto.maxPlaces()) throw new IllegalArgumentException(
                "It is not allowed to set the maxPlaces greater than " +
                        "the location capacity (%s)".formatted(location.capacity())
        );

        CompactEventReadDto updated = Optional.of(event)
                .map(entity -> eventCreateEditMapper.map(createEditDto, entity))
                .map(eventRepository::saveAndFlush)
                .map(compactEventReadMapper::map)
                .orElseThrow();

        List<Long> attendeeIds = userRepository.findByAttendedEventsId(updated.id()).stream()
                .map(userReadMapper::map)
                .map(UserReadDto::id)
                .toList();

        EventKafkaMessage eventKafkaMessage = new EventKafkaMessage(updated.id(),
                authenticatedUser.getId(),
                LocalDateTime.now(),
                updated.ownerId(),
                EventModificationType.UPDATED);
        eventKafkaMessage.addAttendeeIds(attendeeIds);
        eventKafkaMessage.extractFieldChanges(toUpdate, updated);
        eventKafkaMessageSender.send(eventKafkaMessage);

        return updated;
    }

    public void cancel(Long id, AuthenticatedUser authenticatedUser) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Event with id=%s not found".formatted(id)
                ));
        boolean notAdminOrOwner = !(authenticatedUser.getAuthorities().contains(Role.ADMIN) ||
                event.getOwner().getId().equals(authenticatedUser.getId()));
        if (notAdminOrOwner) throw new AuthorizationDeniedException("Access Denied");

        CompactEventReadDto toCancel = compactEventReadMapper.map(event);

        CompactEventReadDto canceled = Optional.of(event)
                .filter(entity -> entity.getStatus().equals(EventStatus.WAIT_START))
                .map(entity -> {
                    entity.setStatus(EventStatus.CANCELLED);
                    return entity;
                })
                .map(eventRepository::saveAndFlush)
                .map(compactEventReadMapper::map)
                .orElseThrow(() -> new IllegalStateException(
                        "It is not allowed to cancel an event that has already started or cancelled"));

        EventKafkaMessage eventKafkaMessage = new EventKafkaMessage(canceled.id(),
                authenticatedUser.getId(),
                LocalDateTime.now(),
                canceled.ownerId(),
                EventModificationType.UPDATED);
        eventKafkaMessage.extractFieldChanges(toCancel, canceled);
        eventKafkaMessageSender.send(eventKafkaMessage);
    }

    public Page<EventReadDto> findAllByOwner(Long ownerId, Pageable pageable) {
        if (userRepository.existsById(ownerId)) {
            return eventRepository.findAllByOwnerId(ownerId, pageable)
                    .map(eventReadMapper::map);
        } else throw new EntityNotFoundException("Authenticated user with id=%s not found".formatted(ownerId));
    }

    public Page<EventReadDto> findAllByVisitor(Long visitorId, Pageable pageable) {
        if (userRepository.existsById(visitorId)) {
            return eventRepository.findAllByVisitorId(visitorId, pageable)
                    .map(eventReadMapper::map);
        } else throw new EntityNotFoundException("Authenticated user with id=%s not found".formatted(visitorId));
    }

    public boolean registerVisitor(Long visitorId, Long eventId) {
        UserEntity visitor = userRepository.findById(visitorId).orElseThrow(
                () -> new EntityNotFoundException("Authenticated user with id=%s not found".formatted(visitorId))
        );
        EventEntity event = eventRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException("Event with id=%s not found".formatted(eventId)));

        if (event.getStatus().equals(EventStatus.FINISHED) || event.getStatus().equals(EventStatus.CANCELLED)) {
            throw new IllegalStateException(
                    "It is not allowed to register for event that has already finished or canceled"
            );
        }
        if (event.getMaxPlaces().equals(event.getOccupiedPlaces())) {
            throw new IllegalStateException(
                    "It is not allowed to register for event that is full"
            );
        }

        if (visitor.addEvent(event)) {
            userRepository.flush();
            eventRepository.flush();
            return true;
        }
        return false;
    }

    public boolean cancelRegistration(Long visitorId, Long eventId) {
        UserEntity visitor = userRepository.findById(visitorId).orElseThrow(
                () -> new EntityNotFoundException("Authenticated user with id=%s not found".formatted(visitorId))
        );
        EventEntity event = eventRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException("Event with id=%s not found".formatted(eventId)));

        if (event.getStatus().equals(EventStatus.FINISHED) || event.getStatus().equals(EventStatus.STARTED)) {
            throw new IllegalStateException(
                    "It is not allowed to cancel registration for event that has already finished or started"
            );
        }

        if (visitor.removeEvent(event)) {
            userRepository.flush();
            eventRepository.flush();
            return true;
        }
        return false;
    }
}
