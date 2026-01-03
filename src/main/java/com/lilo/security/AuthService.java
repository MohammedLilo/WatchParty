package com.lilo.security;

import com.lilo.enums.ERoles;
import com.lilo.exception.JwtAuthenticationException;
import com.lilo.model.DefaultUserProfilePicture;
import com.lilo.model.Role;
import com.lilo.model.User;
import com.lilo.operationResult.TableOperationResult;
import com.lilo.repository.RolesRepository;
import com.lilo.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RolesRepository rolesRepository;
    private final JwtService jwtService;
    private Map<ERoles, Role> cachedRoles;
    private final DefaultUserProfilePictureService defaultUserProfilePictureService;

    @PostConstruct
    public void init() {
        cachedRoles = rolesRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getName, Function.identity()));
    }

    public TableOperationResult save(User user) {
        user.setEmail(user.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        DefaultUserProfilePicture defaultUserProfilePicture = defaultUserProfilePictureService.findRandomly().orElseThrow(()-> {
            return new RuntimeException("CRITICAL ERROR: Default profile picture was not found");
        });
        user.setProfilePicture(defaultUserProfilePicture.getPictureFileName());

        Role defaultRole = cachedRoles.get(ERoles.USER);

        if (defaultRole == null) {
            log.error("CRITICAL ERROR: Default role '{}' not found in cache.", ERoles.USER.name());
            return TableOperationResult.fromFailure("System configuration error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        user.setRoles(Set.of(defaultRole));
        return userService.save(user);
    }

    public Optional<User> validateUserCredentials(String email, String rawPassword) {
        Optional<User> storedUser = userService.findByEmail(email);

        if (storedUser.isEmpty() || !isValidPassword(storedUser.get(), rawPassword))
            return Optional.empty();
        return storedUser;
    }

    public String generateToken(User user) {
        return jwtService.generateToken(user);
    }

    private boolean isValidPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public long getUserId(String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        return jwtService.extractId(token);
    }

    public Authentication authenticateUserFromToken(String jwt) {
        if (!jwtService.validateToken(jwt)) {
            log.warn("JWT validation failed for token.");
            throw new JwtAuthenticationException("Invalid or expired token");
        }

        long userId = jwtService.extractId(jwt);
        User storedUser = userService.findById(userId)
                                     .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                storedUser,
                null,
                storedUser.getAuthorities()
        );

        log.info("Authenticated user {} from token for WebSocket.", storedUser.getId());
        return authentication;
    }

//    /**
//     * Authenticates a user connecting via WebSocket STOMP.
//     * Reads the token from the headers, validates it, and sets the
//     * authenticated Principal on the session.
//     *
//     * @param stompHeaderAccessor The StompHeaderAccessor for the connection.
//     */
//    public void authenticateWebSocketUser(StompHeaderAccessor stompHeaderAccessor) {
//        // Extract the token from the "native" headers (the original HTTP handshake headers)
//        String authHeader = stompHeaderAccessor.getFirstNativeHeader("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            log.warn("WebSocket CONNECT attempt with missing or invalid Authorization header.");
//            throw new UnauthorizedAccessException("Missing or invalid Authorization header");
//        }
//        String jwt = authHeader.substring(7);
//        if (jwtService.validateToken(jwt)) {
//            tryAuthenticate(jwt, stompHeaderAccessor);
//        } else
//            throw new JwtAuthenticationException("Invalid or expired token");
//    }
//
//    private void tryAuthenticate(String jwt, StompHeaderAccessor stompHeaderAccessor) {
//        long userId = jwtService.extractId(jwt);
//        User storedUser = userService.findById(userId)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                storedUser,
//                null,
//                storedUser.getAuthorities()
//        );
//
//        stompHeaderAccessor.setUser(authentication);
//
//        log.info("Authenticated user {} for WebSocket session.", storedUser.getId());
//    }
}