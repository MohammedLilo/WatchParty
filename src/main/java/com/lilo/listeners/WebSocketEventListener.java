package com.lilo.listeners;

import com.lilo.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

//    @EventListener
//    public void handleWebSocketConnect(SessionConnectEvent event) {
//    }

    @EventListener
    public void handleWebSocketConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (!(principal instanceof Authentication)) {
            log.error("User principal not found or no Authentication in SessionConnectedEvent.");
            return;
        }

        User user = (User) ((Authentication) principal).getPrincipal();
        if (user == null) {
            log.error("User object casted from Principal is null.");
            return;
        }

        String sessionId = accessor.getSessionId();
        log.info("User {} connected. Session ID: {}", user.getId(), sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();

        if (principal instanceof Authentication authentication) {
            User user = (User) authentication.getPrincipal();
            if (user != null) {
                log.info("User {} disconnected. Session ID: {}", user.getId(), sessionId);
            }
        } else {
            log.info("Session ID {} disconnected (user not established).", sessionId);
        }
    }


    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();
        String subscriptionDestination = accessor.getDestination();

        if (principal instanceof Authentication authentication) {
            User user = (User) authentication.getPrincipal();
            if (user != null)
                log.info("User {} with Session ID: {}, subscribed to {}", user.getId(), sessionId, subscriptionDestination);
        }
    }


}
