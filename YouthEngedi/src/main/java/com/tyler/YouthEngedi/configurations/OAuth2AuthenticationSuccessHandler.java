package com.tyler.YouthEngedi.configurations;

import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.annotations.LogExecutionTime;
import com.tyler.YouthEngedi.jwts.JwtTokenProvider;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.enums.AuthProvider;
import com.tyler.YouthEngedi.services.CookieService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CookieService cookieService;

    @Override
    @LogExecutionTime(value = "Calling onAuthenticationSuccess() in OAuth2AuthenticationSuccessHandler class",doSave = false)
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        if (oAuth2User == null) {
            handleExceptionRedirect(request, response, "authentication_failed");
            return;
        }

        // Extract attributes safely
        String email = oAuth2User.getAttribute("email");
        String dateOfBirth = oAuth2User.getAttribute("birthdate");
        String profileImageUrl = oAuth2User.getAttribute("picture");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            handleExceptionRedirect(request, response, "email_not_found_from_provider");
            return;
        }

        Optional<User> oAuthToUser = userRepository.findByEmail(email);

        User user;

        if (oAuthToUser.isPresent()) {
            user = oAuthToUser.get();

            // Check if account is disabled safely
            if (!user.isEnabled()) {
                handleExceptionRedirect(request, response, "account_disabled");
                return;
            }

            user.setName(name);
            user.setDateOfBirth(dateOfBirth);
            user.setProfileImageUrl(profileImageUrl);

            user = userRepository.save(user);

        } else {
            User newUser = User.builder()
                    .email(email)
                    .dateOfBirth(dateOfBirth)
                    .createdAt(LocalDateTime.now())
                    .profileImageUrl(profileImageUrl)
                    .authProvider(AuthProvider.OAUTH2)
                    .enabled(true)
                    .name(name)
                    .build();

            user = userRepository.save(newUser);
        }

        issueTokenAndRedirect(request, response, user);
    }

    private void issueTokenAndRedirect(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        String token = tokenProvider.generateToken(user);

        response.addHeader(HttpHeaders.SET_COOKIE,cookieService.issueToken(token));

        getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/oauth2/redirect");
    }

    private void handleExceptionRedirect(HttpServletRequest request, HttpServletResponse response, String errorCode) throws IOException {
        // Redirect back to frontend login with an error query parameter instead of breaking the filter chain
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/login").queryParam("error", errorCode).build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}