package com.itdev.finalproject.service;

import com.itdev.finalproject.database.entity.Role;
import com.itdev.finalproject.database.repository.UserRepository;
import com.itdev.finalproject.dto.createedit.UserCreateEditDto;
import com.itdev.finalproject.dto.read.UserReadDto;
import com.itdev.finalproject.mapper.createedit.UserCreateEditMapper;
import com.itdev.finalproject.mapper.read.UserReadMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserCreateEditMapper userCreateEditMapper;
    private final UserReadMapper userReadMapper;

    public UserService(UserRepository userRepository, UserCreateEditMapper userCreateEditMapper, UserReadMapper userReadMapper) {
        this.userRepository = userRepository;
        this.userCreateEditMapper = userCreateEditMapper;
        this.userReadMapper = userReadMapper;
    }

    public Page<UserReadDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userReadMapper::map);
    }

    public Optional<UserReadDto> findById(Long id) {
        return userRepository.findById(id)
                .map(userReadMapper::map);
    }

    @Transactional
    public UserReadDto create(UserCreateEditDto createEditDto) {
        return Optional.of(createEditDto)
                .map(userCreateEditMapper::map).stream()
                .peek(entity -> entity.setRole(Role.USER)).findFirst()
                .map(userRepository::save)
                .map(userReadMapper::map)
                .orElseThrow();
    }

    @Transactional
    public Optional<UserReadDto> update(Long id, UserCreateEditDto createEditDto) {
        return userRepository.findById(id)
                .map(entity -> userCreateEditMapper.map(createEditDto, entity))
                .map(userRepository::saveAndFlush)
                .map(userReadMapper::map);
    }

    @Transactional
    public boolean delete(Long id) {
        return userRepository.findById(id)
                .map(entity -> {
                    userRepository.delete(entity);
                    userRepository.flush();
                    return true;
                })
                .orElse(false);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.singleton(user.getRole())
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Fail to retrieve user: " + username));
    }

    public UserReadDto findByUsername(String username) {
        return Optional.of(username)
                .flatMap(userRepository::findByUsername)
                .map(userReadMapper::map)
                .orElseThrow(EntityNotFoundException::new);
    }
}
