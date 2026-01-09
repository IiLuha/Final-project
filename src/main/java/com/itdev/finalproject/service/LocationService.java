package com.itdev.finalproject.service;

import com.itdev.finalproject.database.repository.LocationRepository;
import com.itdev.finalproject.dto.createedit.LocationCreateEditDto;
import com.itdev.finalproject.dto.read.LocationReadDto;
import com.itdev.finalproject.mapper.createedit.LocationCreateEditMapper;
import com.itdev.finalproject.mapper.read.LocationReadMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationCreateEditMapper locationCreateEditMapper;
    private final LocationReadMapper locationReadMapper;

    public LocationService(LocationRepository locationRepository, LocationCreateEditMapper locationCreateEditMapper, LocationReadMapper locationReadMapper) {
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

    public LocationReadDto create(LocationCreateEditDto createEditDto) {
        return Optional.of(createEditDto)
                .map(locationCreateEditMapper::map)
                .map(locationRepository::save)
                .map(locationReadMapper::map)
                .orElseThrow();
    }

    public Optional<LocationReadDto> update(Long id, LocationCreateEditDto createEditDto) {
        return locationRepository.findById(id)
                .map(entity -> locationCreateEditMapper.map(createEditDto, entity))
                .map(locationRepository::saveAndFlush)
                .map(locationReadMapper::map);
    }

    public boolean delete(Long id) {
        return locationRepository.findById(id)
                .map(entity -> {
                    locationRepository.delete(entity);
                    locationRepository.flush();
                    return true;
                })
                .orElse(false);
    }
}
