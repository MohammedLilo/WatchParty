package com.lilo.security;

import com.lilo.exception.JwtAuthenticationException;
import com.lilo.service.UserService;
import com.lilo.shared.SecurityConstants;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    public JwtFilter(UserDetailsService userDetailsService, JwtService jwtService, UserService userService, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (isTokenPresent(authHeader)) {
            token = authHeader.substring(7);
            try {
                long id = jwtService.extractId(token);

                if (isUserNotAuthenticated()) {
                    tryAuthenticateUser(id, request, response);
                }
            } catch (JwtException | UsernameNotFoundException e) {
                SecurityContextHolder.clearContext();
                jwtAuthenticationEntryPoint.commence(request, response, new JwtAuthenticationException("Invalid or expired token"));
                return;
            }
        } else {
            jwtAuthenticationEntryPoint.commence(request, response, new JwtAuthenticationException("Token is missing"));
            return;
        }
        filterChain.doFilter(request, response);
    }
    private void tryAuthenticateUser(long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserDetails storedUserDetails = userService.findById(id)
                                                   .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                storedUserDetails,
                null,
                storedUserDetails.getAuthorities());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

//    private void tryAuthenticateUser(String email, HttpServletRequest request, HttpServletResponse response) throws IOException {
//        UserDetails storedUserDetails = userDetailsService.loadUserByUsername(email);
//        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                storedUserDetails,
//                null,
//                storedUserDetails.getAuthorities());
//
//        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//    }

    private boolean isTokenPresent(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

    private boolean isUserNotAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestPath = request.getRequestURI();
        return Arrays.stream(SecurityConstants.getPublicEndpoints()).anyMatch(endpoint -> pathMatcher.match(endpoint, requestPath));
    }
}
