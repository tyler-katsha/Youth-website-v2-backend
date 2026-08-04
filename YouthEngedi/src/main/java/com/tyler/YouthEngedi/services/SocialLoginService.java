package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.VerificationException;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.annotations.LogExecutionTime;
import com.tyler.YouthEngedi.jwts.JwtTokenProvider;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.SocialLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final CookieService cookieService;

    @LogExecutionTime(value="Login with OAuth2.0 authentication",doSave = false)
    public ResponseEntity<?> socialLogin(SocialLoginRequest request){

        String email = verifyAndGetEmail(request);

        User user = userRepository.findByEmail(email).orElseGet(() -> registerNewUser(email));
        
        String token = tokenProvider.generateToken(user);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookieService.issueToken(token)).body("Login Successfully");
    }

    private String verifyAndGetEmail(SocialLoginRequest request) {
        return switch (request.getProvider().toUpperCase()){
            case "GOOGLE" -> verifyGoogle(request.getToken());
            case "FACEBOOK" -> verifyFacebook(request.getToken());
            case "INSTAGRAM" -> verifyInstagram(request.getToken());
            default -> throw new IllegalArgumentException("Unknown Provider");
        };
    }

    @LogExecutionTime(value="Verifying Google account in SocialLoginService class",doSave = false)
    private String verifyGoogle(String token) {
        try{
            String url = "https://www.googleapis.com/oauth2/v3/userinfo";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            Map response = restTemplate.exchange(url, HttpMethod.GET,entity,Map.class).getBody();

            if(response == null && !response.containsKey("email")) {
                throw new VerificationException("Email not found in Google response");
            }

            return response.get("email").toString();
        } catch (Exception e){
            throw new VerificationException("Unable to verify Google account: " + e.getMessage());
        }
    }

    @LogExecutionTime(value="Verifying Facebook account in SocialLoginService class",doSave = false)
    private String verifyFacebook(String token) {
        try{
            String url = "https://graph.facebook.com/me?fields=email,name&access_token="+token;
            return Objects.requireNonNull(restTemplate.getForObject(url, Map.class)).get("email").toString();
        } catch (NullPointerException e){
            e.printStackTrace();
            throw new VerificationException("Unable to verify Facebook account");
        }
    }

    @LogExecutionTime(value="Verifying Instagram account in SocialLoginService class",doSave = false)
    private String verifyInstagram(String token) {
        try{
            String url = "https://graph.instagram.com/me?fields=email,name&access_token="+token;
            return Objects.requireNonNull(restTemplate.getForObject(url, Map.class).get("username").toString());
        } catch (NullPointerException e){
            e.printStackTrace();
            throw new VerificationException("Unable to verify Instagram account");
        }
    }

    private User registerNewUser(String email){

        User newUser = User
                .builder()
                .email(email)
                .name(email.split("@")[0])
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(newUser);
    }
}
