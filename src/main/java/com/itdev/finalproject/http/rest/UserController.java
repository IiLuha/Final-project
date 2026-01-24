package com.itdev.finalproject.http.rest;

import com.itdev.finalproject.database.entity.Role;
import com.itdev.finalproject.dto.AuthenticatedUser;
import com.itdev.finalproject.dto.JwtResponse;
import com.itdev.finalproject.dto.SignInDto;
import com.itdev.finalproject.dto.createedit.UserCreateEditDto;
import com.itdev.finalproject.dto.read.UserReadDto;
import com.itdev.finalproject.service.JwtAuthenticationService;
import com.itdev.finalproject.service.UserService;
import com.itdev.finalproject.validation.group.CreateAction;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
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
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService userService;
    private final JwtAuthenticationService jwtService;

    public UserController(UserService userService, JwtAuthenticationService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<Page<UserReadDto>> findAll(Pageable pageable) {
        Page<UserReadDto> users = userService.findAll(pageable);
        return users.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public UserReadDto findById(@PathVariable("id") Long id) {
        return userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/auth")
    public ResponseEntity<JwtResponse> authentication(
            @RequestBody @Valid SignInDto user) {
        var token = jwtService.authenticateUser(user);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/register")
    public UserReadDto register(
            @RequestBody @Validated({Default.class, CreateAction.class}) UserCreateEditDto user) {
        return userService.create(user);
    }

    @PutMapping("/{id}")
    public UserReadDto update(@PathVariable Long id,
                              @Valid @RequestBody UserCreateEditDto editDto,
                              @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.getAuthorities().contains(Role.ADMIN) || id.equals(authenticatedUser.getId())) {
            return userService.update(id, editDto)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        } else throw new AuthorizationDeniedException("Access Denied");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!userService.delete(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
