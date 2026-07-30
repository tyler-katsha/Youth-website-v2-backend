package com.tyler.YouthEngedi.filters;

import com.tyler.YouthEngedi.models.AdminEvent;
import com.tyler.YouthEngedi.models.enums.ConnectionType;
import com.tyler.YouthEngedi.sockets.AdminEventPublisher;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TrafficFilter extends OncePerRequestFilter {

    @Autowired
    private AdminEventPublisher publisher;
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        filterChain.doFilter(request,response);

        publisher.publish(AdminEvent
                .builder()
                .type(ConnectionType.REQUEST)
                .userId("SYSTEM")
                .message(request.getMethod() + " " + request.getRequestURI())
                .timestamp(System.currentTimeMillis())
                .build());
    }
}
