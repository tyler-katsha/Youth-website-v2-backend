package com.tyler.YouthEngedi.models;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class UserPrincipal extends User {
    private final long userId;

    public UserPrincipal(long userId,String username, @Nullable String password, Collection<? extends GrantedAuthority> authorities) {
        super(username,password,authorities);

        this.userId = userId;
    }

}
