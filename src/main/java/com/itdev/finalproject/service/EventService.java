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
import com.itdev.finalproject.dto.read.EventReadDto;
import com.itdev.finalproject.dto.read.LocationReadDto;
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

import java.util.Optional;
import java.util.stream.Stream;

@Service
@Transactional
public class EventService{

    private final LocationService locationService;
    private final EventRepository eventRepository;
    private final EventCreateEditMapper eventCreateEditMapper;
    private final EventReadMapper eventReadMapper;
    private final EventJpaPredicateBuilder eventJpaPredicateBuilder;
    private final UserRepository userRepository;

    public EventService(LocationService locationService,
                        EventRepository eventRepository,
                        EventCreateEditMapper eventCreateEditMapper,
                        EventReadMapper eventReadMapper,
                        EventJpaPredicateBuilder eventJpaPredicateBuilder,
                        UserRepository userRepository) {
        this.locationService = locationService;
        this.eventRepository = eventRepository;
        this.eventCreateEditMapper = eventCreateEditMapper;
        this.eventReadMapper = eventReadMapper;
        this.eventJpaPredicateBuilder = eventJpaPredicateBuilder;
        this.userRepository = userRepository;
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

    public EventReadDto create(EventCreateEditDto createEditDto, AuthenticatedUser authenticatedUser) {
        UserEntity owner = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Authenticated user with id=%s not found" .formatted(authenticatedUser.getId())));
        LocationReadDto location = locationService.findById(createEditDto.locationId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Location with id=%s not found" .formatted(createEditDto.locationId())));

        if (location.capacity() < createEditDto.maxPlaces()) throw new IllegalArgumentException(
                "It is not allowed to set the maxPlaces greater than " +
                        "the location capacity (%s)".formatted(location.capacity())
        );

        return Optional.of(createEditDto)
                .map(eventCreateEditMapper::map).stream()
                .peek(event -> event.setStatus(EventStatus.WAIT_START))
                .peek(event -> event.setOccupiedPlaces(0))
                .peek(event -> event.setOwner(owner)).findFirst()
                .map(eventRepository::save)
                .map(eventReadMapper::map)
                .orElseThrow();
    }

    public EventReadDto update(Long id, EventCreateEditDto createEditDto, AuthenticatedUser authenticatedUser) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        LocationReadDto location = locationService.findById(createEditDto.locationId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Location with id=%s not found" .formatted(createEditDto.locationId())));
        if (authenticatedUser.getAuthorities().contains(Role.ADMIN) ||
                event.getOwner().getId().equals(authenticatedUser.getId())) {

            if (createEditDto.maxPlaces() < event.getOccupiedPlaces()) throw new IllegalArgumentException(
                    "It is not allowed to set the maxPlaces less than " +
                        "the number of already registered users (%s)".formatted(event.getOccupiedPlaces())
            );
            if (location.capacity() < createEditDto.maxPlaces()) throw new IllegalArgumentException(
                    "It is not allowed to set the maxPlaces greater than " +
                            "the location capacity (%s)".formatted(location.capacity())
            );

            return Optional.of(event)
                    .map(entity -> eventCreateEditMapper.map(createEditDto, entity))
                    .map(eventRepository::saveAndFlush)
                    .map(eventReadMapper::map)
                    .orElseThrow();
        } else throw new AuthorizationDeniedException("Access Denied");
    }

    public void cancel(Long id, AuthenticatedUser authenticatedUser) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Event with id=%s not found".formatted(id)
                ));
        if (authenticatedUser.getAuthorities().contains(Role.ADMIN) ||
                event.getOwner().getId().equals(authenticatedUser.getId())) {
            Stream.of(event)
                    .filter(entity -> entity.getStatus().equals(EventStatus.WAIT_START))
                    .peek(entity -> entity.setStatus(EventStatus.CANCELLED)).findFirst()
                    .ifPresentOrElse(
                            eventRepository::saveAndFlush,
                            () -> {throw new IllegalStateException(
                                    "It is not allowed to cancel an event that has already started or cancelled"
                            );}
                    );
        } else throw new AuthorizationDeniedException("Access Denied");
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
