package com.tyler.YouthEngedi.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
@Schema(description = "Allows Spring security to inject and store user information inside the user")
public class UserPrincipal extends User {
    @Schema(description = "Allows to fetch the userId from jwt token",example = "1")
    private final long userId;

    public UserPrincipal(long userId,String username, @Nullable String password, Collection<? extends GrantedAuthority> authorities) {
        super(username,password,authorities);
        this.userId = userId;
    }

}
