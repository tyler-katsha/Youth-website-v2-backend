package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.enums.AuthProvider;
import com.tyler.YouthEngedi.models.enums.Role;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @NullMarked
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        // fetch data from external providers
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String imageUrl = oAuth2User.getAttribute("picture"); // picture or avatar_url
        String dateOfBirth = oAuth2User.getAttribute("birthdate") != null ? oAuth2User.getAttribute("birthdate") : null;

        User user = userRepository.findByEmail(email).orElseGet(() -> User
                .builder()
                .name(name)
                .email(email)
                .authProvider(AuthProvider.OAUTH2)
                .roles(Set.of(Role.MEMBER))
                .build());

        user.setProfileImageUrl(imageUrl);

        if(dateOfBirth != null) user.setDateOfBirth(dateOfBirth);

        userRepository.save(user);

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),oAuth2User.getAttributes(),"email");
    }
}
