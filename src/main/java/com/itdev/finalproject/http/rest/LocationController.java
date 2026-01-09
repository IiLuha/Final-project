package com.itdev.finalproject.http.rest;

import com.itdev.finalproject.dto.PageResponse;
import com.itdev.finalproject.dto.createedit.LocationCreateEditDto;
import com.itdev.finalproject.dto.read.LocationReadDto;
import com.itdev.finalproject.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("api/v1/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public PageResponse<LocationReadDto> findAll(Pageable pageable) {
        return PageResponse.of(locationService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public LocationReadDto findById(@PathVariable("id") Long id) {
        return locationService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/create")
    public LocationReadDto  create(@RequestBody @Valid LocationCreateEditDto location) {
        return locationService.create(location);
    }

    @PutMapping("/{id}")
    public LocationReadDto update(@PathVariable Long id, @Validated @RequestBody LocationCreateEditDto editDto) {
        return locationService.update(id, editDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!locationService.delete(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
