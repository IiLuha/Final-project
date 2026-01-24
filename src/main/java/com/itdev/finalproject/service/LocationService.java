package com.itdev.finalproject.service;

import com.itdev.finalproject.database.entity.LocationEntity;
import com.itdev.finalproject.database.repository.EventRepository;
import com.itdev.finalproject.database.repository.LocationRepository;
import com.itdev.finalproject.dto.createedit.LocationCreateEditDto;
import com.itdev.finalproject.dto.read.LocationReadDto;
import com.itdev.finalproject.mapper.createedit.LocationCreateEditMapper;
import com.itdev.finalproject.mapper.read.LocationReadMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class LocationService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final LocationCreateEditMapper locationCreateEditMapper;
    private final LocationReadMapper locationReadMapper;

    public LocationService(EventRepository eventRepository,
                           LocationRepository locationRepository,
                           LocationCreateEditMapper locationCreateEditMapper,
                           LocationReadMapper locationReadMapper) {
        this.eventRepository = eventRepository;
        this.locationRepository = locationRepository;
        this.locationCreateEditMapper = locationCreateEditMapper;
        this.locationReadMapper = locationReadMapper;
    }

    public Page<LocationReadDto> findAll(Pageable pageable) {
        return locationRepository.findAll(pageable)
                .map(locationReadMapper::map);
    }

    public Optional<LocationReadDto> findById(Long id) {
        return locationRepository.findById(id)
                .map(locationReadMapper::map);
    }

    @Transactional
    public LocationReadDto create(LocationCreateEditDto createEditDto) {
        if (locationRepository.existsByName(createEditDto.name())) throw new IllegalArgumentException(
                "Location with name=%s already exist".formatted(createEditDto.name())
        );
        return Optional.of(createEditDto)
                .map(locationCreateEditMapper::map)
                .map(locationRepository::save)
                .map(locationReadMapper::map)
                .orElseThrow();
    }

    @Transactional
    public LocationReadDto update(Long id, LocationCreateEditDto createEditDto) {
        LocationEntity location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location with id=%s not found".formatted(id)));
        if (createEditDto.capacity() < location.getCapacity()) throw new IllegalArgumentException(
                "It is not allowed to set the new capacity less than the old capacity (%s)"
                        .formatted(location.getCapacity())
        );
        locationCreateEditMapper.map(createEditDto, location);
        locationRepository.saveAndFlush(location);
        return locationReadMapper.map(location);
    }

    @Transactional
    public void delete(Long id) {
        LocationEntity location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location with id=%s not found".formatted(id)));
        if (eventRepository.existsByLocation(location)) throw new IllegalStateException(
                "It is not allowed to delete an location for which an event has already been registered"
        );
        locationRepository.delete(location);
        locationRepository.flush();

    }
}
