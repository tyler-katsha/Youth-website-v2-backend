package com.tyler.YouthEngedi.jwts;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secretKey;
    @Value("${app.jwt.expiration-milliseconds}")
    private long jwtExpiration;
    @Autowired
    private UserRepository userRepository;

    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId",user.getId());
        claims.put("isDeleted",user.isDeleted());
        claims.put("roles",user.getRoles());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getKey())
                .compact();
    }

    public String generateToken(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist"));

        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",user.getId());
        claims.put("isDeleted",user.isDeleted());
        claims.put("roles",user.getRoles());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getKey())
                .compact();
    }

    public String generateToken(User user, int time) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId",user.getId());
        claims.put("isDeleted",user.isDeleted());
        claims.put("roles",user.getRoles());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .signWith(getKey())
                .compact();
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public long extractUserId(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("userId",Long.class);
    }
    public Role extractRole(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("role", Role.class);
    }
    public boolean extractIsDeleted(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("isDeleted",Boolean.class);
    }
    private <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token, UserDetails userDetails) throws ExpiredJwtException {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token,Claims::getExpiration);
    }

    public long getRemainingSessionTimeInSeconds(String token){
        try{

            Date expiration = extractExpiration(token);

            long remainingTimeMillis = expiration.getTime() - System.currentTimeMillis();

            return Math.max(0, remainingTimeMillis/1000);
        } catch(ExpiredJwtException e){
            return 0;
        } catch(Exception e){
            return 0;
        }
    }
}
