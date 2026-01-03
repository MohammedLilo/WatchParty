package com.lilo.listeners;

import com.lilo.enums.PartyMemberEvent;
import com.lilo.model.User;
import com.lilo.model.dto.PartyMemberEventOutputDTO;
import com.lilo.service.PartiesService;
import com.lilo.shared.WebSocketConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final PartiesService partiesService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final TaskScheduler taskScheduler;
    // Tracks active "countdown" timers for disconnected users
    private final Map<Long, ScheduledFuture<?>> pendingLeaveTasks = new ConcurrentHashMap<>();
    @Value("${watchparty.security.grace-period-seconds:180}")
    private int gracePeriodSeconds;

        @EventListener
    public void onConnect(SessionConnectEvent event) {
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
            long userId = user.getId();
            ScheduledFuture<?> future = pendingLeaveTasks.remove(userId);
            if (future != null) {
                future.cancel(false);
                System.out.println("User " + userId + " reconnected! Grace period aborted.");
            }

        }

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
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
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();

        if (principal instanceof Authentication authentication) {
            User authenticatedUser = (User) authentication.getPrincipal();
            if (authenticatedUser != null) {
                long userId = authenticatedUser.getId();

                ScheduledFuture<?> task = taskScheduler.schedule(() -> {
                    log.info("User {} disconnected. Session ID: {}", authenticatedUser.getId(), sessionId);

                    partiesService.processUserLeave(authenticatedUser);
                    simpMessagingTemplate.convertAndSend(WebSocketConstants.TOPIC_PARTY_MEMBER_EVENTS, new PartyMemberEventOutputDTO(authenticatedUser.getId(), PartyMemberEvent.LEFT, Instant.now()));

                    pendingLeaveTasks.remove(userId);
                }, Instant.now().plus(gracePeriodSeconds, ChronoUnit.SECONDS));

                pendingLeaveTasks.put(userId, task);
            }
        } else {
            log.info("Session ID {} disconnected (user not established).", sessionId);
        }
    }


    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
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
