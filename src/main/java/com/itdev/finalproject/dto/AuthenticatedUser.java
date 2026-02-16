package com.itdev.finalproject.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itdev.finalproject.database.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class AuthenticatedUser extends User {

    private final Long id;

    @JsonCreator
    public AuthenticatedUser(
            @JsonProperty("id") Long id,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("authorities") Collection<? extends Role> authorities) {
        super(username, password, authorities);
        this.id = id;
    }

    public AuthenticatedUser(Long id, String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
