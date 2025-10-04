package com.lilo.interceptors;

import com.lilo.model.User;
import com.lilo.security.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final AuthService authService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {


        String token = null;

        // 1. Try to get the token from the query parameter
        List<String> tokenParams = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().get("token");

        if (tokenParams != null && !tokenParams.isEmpty()) {
            token = tokenParams.getFirst();
            log.debug("Found WebSocket token in query parameter.");
        }

        // 2. If not in query, fall back to the Authorization header
        if (token == null) {
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                log.info("Found WebSocket token in Authorization header.");
            } else {
                log.warn("WebSocket handshake rejected. No token found in query param 'token' or Authorization header.");
                return false; // Reject
            }
        }

        // 4. Authenticate the user
        try {
            Authentication authenticatedUser = authService.authenticateUserFromToken(token);
            attributes.put("user", authenticatedUser); // Make principal available to STOMP
            log.info("WebSocket handshake approved for user: {}", authenticatedUser.getName());
            return true; // Approve handshake

        } catch (Exception e) {
            log.warn("WebSocket handshake rejected due to authentication failure: {}", e.getMessage());
            return false; // Reject
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}