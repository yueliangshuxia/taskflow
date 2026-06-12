package com.taskflow.config;

import com.taskflow.dao.ApiTokenRepository;
import com.taskflow.entity.ApiToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiTokenFilter extends OncePerRequestFilter {

    private final ApiTokenRepository apiTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check for API token in Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            apiTokenRepository.findByToken(token).ifPresent(apiToken -> {
                apiToken.setLastUsedAt(LocalDateTime.now());
                apiTokenRepository.save(apiToken);

                var authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + apiToken.getUser().getRole().name())
                );
                var auth = new UsernamePasswordAuthenticationToken(
                        apiToken.getUser(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }

        filterChain.doFilter(request, response);
    }
}
