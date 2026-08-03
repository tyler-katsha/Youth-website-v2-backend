package com.tyler.YouthEngedi.configurations;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.tyler.YouthEngedi.constants.UrlConstants.FRONTEND_LOGIN_PROD;

@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, AuthenticationException exception) throws IOException {
        String error = "oauth_failed";

        if(exception.getMessage().contains("account_disabled")){
            error = "account_disabled";
        } else if (exception.getMessage().contains("Email not verified")){
            error = "email_not_verified";
        }

        String link = FRONTEND_LOGIN_PROD + "?error=" + error;
        getRedirectStrategy().sendRedirect(request,response,link);
    }
}
