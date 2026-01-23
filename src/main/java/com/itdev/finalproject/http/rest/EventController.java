package com.itdev.finalproject.http.rest;

import com.itdev.finalproject.dto.AuthenticatedUser;
import com.itdev.finalproject.dto.ServerErrorDto;
import com.itdev.finalproject.dto.createedit.EventCreateEditDto;
import com.itdev.finalproject.dto.filter.EventFilter;
import com.itdev.finalproject.dto.read.EventReadDto;
import com.itdev.finalproject.service.EventService;
import com.itdev.finalproject.validation.group.CreateAction;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/search")
    public ResponseEntity<Page<EventReadDto>> findAll(
            @RequestBody EventFilter filter,
            Pageable pageable) {
        Page<EventReadDto> events = eventService.findAll(filter, pageable);
        return events.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public EventReadDto findById(@PathVariable("id") Long id) {
        return eventService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public EventReadDto  create(
            @RequestBody @Validated({Default.class, CreateAction.class}) EventCreateEditDto event,
            @AuthenticationPrincipal AuthenticatedUser owner) {
        return eventService.create(event, owner);
    }

    @PutMapping("/{id}")
    public EventReadDto update(@PathVariable Long id,
                               @Valid @RequestBody EventCreateEditDto editDto,
                               @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return eventService.update(id, editDto, authenticatedUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id,
                       @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        eventService.cancel(id, authenticatedUser);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<EventReadDto>> findAllByOwner(
            @AuthenticationPrincipal AuthenticatedUser owner,
            Pageable pageable) {
        Page<EventReadDto> events = eventService.findAllByOwner(owner.getId(), pageable);
        return events.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(events);
    }
    @PostMapping("/registrations/{id}")
    public ResponseEntity<?> create(
            @AuthenticationPrincipal AuthenticatedUser visitor,
            @PathVariable("id") Long eventId) {
        return eventService.registerVisitor(visitor.getId(), eventId) ?
                ResponseEntity.ok().build() : ResponseEntity.badRequest().body(
                        new ServerErrorDto("Bed request",
                                "Authenticated user is already registered for the event with id=" + eventId)
        );
    }
    @DeleteMapping("/registrations/cancel/{id}")
    public ResponseEntity<?> cancelRegistration(
            @AuthenticationPrincipal AuthenticatedUser visitor,
            @PathVariable("id") Long eventId) {
        return eventService.cancelRegistration(visitor.getId(), eventId) ?
                ResponseEntity.noContent().build() : ResponseEntity.badRequest().body(
                new ServerErrorDto("Bed request",
                        "Authenticated user isn't registered for the event with id=" + eventId)
        );
    }
    @GetMapping("/registrations/my")
    public ResponseEntity<Page<EventReadDto>> findAllByVisitor(
            @AuthenticationPrincipal AuthenticatedUser visitor,
            Pageable pageable) {
        Page<EventReadDto> events = eventService.findAllByVisitor(visitor.getId(), pageable);
        return events.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(events);
    }
}
