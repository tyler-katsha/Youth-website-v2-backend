package com.tyler.YouthEngedi.configurations;

import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.annotations.LogExecutionTime;
import com.tyler.YouthEngedi.jwts.JwtTokenProvider;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.enums.AuthProvider;
import com.tyler.YouthEngedi.models.enums.Role;
import com.tyler.YouthEngedi.utils.WebSocketHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

import static com.tyler.YouthEngedi.constants.UrlConstants.*;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    @LogExecutionTime(value = "Calling onAuthenticationSuccess() in OAuth2AuthenticationSuccessHandler class",doSave = false)
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");

        if (email == null || email.isBlank()) {
            handleExceptionRedirect(request, response, "oauth_cancelled");
            return;
        }

        String name = oAuth2User.getAttribute("name");
        String profileImageUrl = oAuth2User.getAttribute("picture");
        String dateOfBirth = oAuth2User.getAttribute("birthdate"); // May be null for Google

        User user = userRepository.findByEmail(email)
                .map(existingUser -> {

                    if (name != null && !name.isBlank()) {
                        existingUser.setName(name);
                    }

                    if(!existingUser.getAuthProvider().equals(AuthProvider.LOCAL)){
                        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
                            existingUser.setProfileImageUrl(profileImageUrl);
                        }
                    }

                    if (dateOfBirth != null && !dateOfBirth.isBlank()) {
                        existingUser.setDateOfBirth(dateOfBirth);
                    }

                    existingUser.setEnabled(true);

                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .profileImageUrl(profileImageUrl)
                                .dateOfBirth(dateOfBirth)
                                .authProvider(AuthProvider.OAUTH2)
                                .enabled(true)
                                .roles(Set.of(Role.MEMBER))
                                .createdAt(LocalDateTime.now())
                                .build()
                ));


        var event = WebSocketHelper.buildLogin(user);

        publisher.publishEvent(event);

        issueTokenAndRedirect(request, response, user);
    }

    private void issueTokenAndRedirect(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        String token = tokenProvider.generateToken(user);

        //response.addHeader(HttpHeaders.SET_COOKIE,cookieService.issueToken(token));

        String baseUrl = production ? FRONTEND_OAUTH_PROD : FRONTEND_OAUTH_DEV;
        String targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("token", token)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void handleExceptionRedirect(HttpServletRequest request, HttpServletResponse response, String errorCode) throws IOException {
        // Redirect back to frontend login with an error query parameter instead of breaking the filter chain
        String targetUrl = UriComponentsBuilder.fromUriString(production ? FRONTEND_LOGIN_PROD : FRONTEND_LOGIN_DEV).queryParam("error", errorCode).build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}