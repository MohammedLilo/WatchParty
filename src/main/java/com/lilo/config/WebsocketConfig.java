package com.lilo.config;

import com.lilo.interceptors.AuthHandshakeInterceptor;
import com.lilo.interceptors.SubscriptionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

import static com.lilo.shared.WebSocketConstants.*;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    private final AuthHandshakeInterceptor authHandshakeInterceptor;
    private final SubscriptionInterceptor subscriptionInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(authHandshakeInterceptor)
                .setHandshakeHandler(new DefaultHandshakeHandler() { // <-- ADD THIS HANDLER
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {
                        // Get the Authentication object from the attributes
                        // (which was put there by our AuthHandshakeInterceptor)
                        return (Authentication) attributes.get("user");
                    }
                })
                .setAllowedOrigins("http://127.0.0.1:5500", "http://localhost:5500", "http://localhost:5173", "http://127.0.0.1:5173", "null")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay(TOPIC_PARTY/*, TOPIC_PARTY_MEMBERS_COUNT*/, TOPIC_PARTY_CHAT, TOPIC_PARTY_MEMBER_EVENTS, QUEUE_ERRORS)
//        registry.enableSimpleBroker(TOPIC_PARTIES, TOPIC_WATCH_PARTY_MEMBERS_COUNT, TOPIC_CHAT);
                .setRelayHost("localhost")
                .setRelayPort(61613);
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        WebSocketMessageBrokerConfigurer.super.configureClientInboundChannel(registration);
        registration.interceptors(subscriptionInterceptor);
    }
}
