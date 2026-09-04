package com.tyler.YouthEngedi.filters;

import com.tyler.YouthEngedi.rateLimiting.FingerprintRateLimiter;
import com.tyler.YouthEngedi.utils.BotFingerprintSignatures;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class TlsFingerprintFilter extends OncePerRequestFilter {

    private final static Logger logger = LoggerFactory.getLogger(TlsFingerprintFilter.class);
    private final static String JA3_HEADER = "X-TLS-JA3";

    private final FingerprintRateLimiter rateLimiter;

    public TlsFingerprintFilter(FingerprintRateLimiter rateLimiter){
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull  FilterChain filterChain) throws ServletException, IOException {
        var ja3 = request.getHeader(JA3_HEADER);
        var userAgent = request.getHeader("User-Agent");

        var path = request.getRequestURI();
        var remoteAddr = request.getRemoteAddr();

        if (path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (remoteAddr.startsWith("10.") || remoteAddr.startsWith("127.") || "0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            filterChain.doFilter(request, response);
            return;
        }

        if(ja3== null || ja3.isBlank()){
            logger.warn("Missing {} header. Direct connection attempt from IP. {}",JA3_HEADER,request.getRemoteAddr());
            filterChain.doFilter(request,response);
            return;
        }

        if(BotFingerprintSignatures.KNOWN_BOT_JA3.contains(ja3)){
            logger.warn("Blocked request with blacklisted bot JA3: {} (UA: {})", ja3, userAgent);
            writeErrorResponse(response, HttpStatus.FORBIDDEN, "Access denied: automated client detected");
            return;
        }

        if (userAgent != null && BotFingerprintSignatures.BROWSER_UA_PATTERN.matcher(userAgent).matches()) {
            if (BotFingerprintSignatures.KNOWN_BOT_JA3.contains(ja3)) {
                logger.warn("UA Spoofing detected! UA: '{}' accompanied by Bot JA3: {}", userAgent, ja3);
                writeErrorResponse(response, HttpStatus.FORBIDDEN, "Client handshake inconsistency");
                return;
            }
        }

        if (!rateLimiter.isAllowed(ja3)) {
            logger.warn("Rate limit exceeded for JA3 fingerprint: {} (IP: {})", ja3, request.getRemoteAddr());
            writeErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded. Please slow down.");
            return;
        }

        request.setAttribute("CLIENT_TLS_JA3",ja3);

        filterChain.doFilter(request,response);
    }
    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }
}
