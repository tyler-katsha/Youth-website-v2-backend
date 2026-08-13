package com.tyler.YouthEngedi.services;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    @Value("${app.jwt.expiration-milliseconds}")
    private long maxAge;

    public static final boolean production = true;

    public Cookie resetToken(){
        Cookie cookie = new Cookie("jwt-token",null);
        cookie.setPath("/");
        cookie.setSecure(production); // set true in prod
        cookie.setMaxAge(0);

        return cookie;
    }

    public String issueToken(String token){
        return ResponseCookie.from("jwt-token", token)
                .httpOnly(true)
                .secure(production) // Set to TRUE in production
                .maxAge(maxAge)
                .path("/")
                .sameSite(production ? "None" : "Lax")
                .build()
                .toString();
    }

    public String issueToken(String token,long maxAge){
        return ResponseCookie.from("jwt-token", token)
                .httpOnly(true)
                .secure(production) // Set to TRUE in production
                .maxAge(maxAge)
                .path("/")
                .sameSite(production ? "None" : "Lax")
                .build()
                .toString();
    }
}
