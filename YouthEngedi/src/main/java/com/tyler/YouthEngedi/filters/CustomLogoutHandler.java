package com.tyler.YouthEngedi.filters;

import com.tyler.YouthEngedi.models.UserPrincipal;
import com.tyler.YouthEngedi.services.CustomUserDetailsService;
import com.tyler.YouthEngedi.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final UserService userService;

    public CustomLogoutHandler(UserService userService){
        this.userService = userService;
    }

    @Override
    public void logout(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response, @Nullable Authentication authentication) {
        if(authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal){
            userService.logout(principal.getUserId());
        }
    }
}
