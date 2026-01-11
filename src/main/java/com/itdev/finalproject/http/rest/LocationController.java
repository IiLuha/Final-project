package com.itdev.finalproject.http.rest;

import com.itdev.finalproject.dto.createedit.LocationCreateEditDto;
import com.itdev.finalproject.dto.read.LocationReadDto;
import com.itdev.finalproject.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Page<LocationReadDto>> findAll(Pageable pageable) {
        Page<LocationReadDto> locations = locationService.findAll(pageable);
        return locations.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(locations);
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
    public LocationReadDto update(@PathVariable Long id, @Valid @RequestBody LocationCreateEditDto editDto) {
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
