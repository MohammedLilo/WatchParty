package com.lilo.interceptors;

import com.lilo.exception.SubscriptionForbiddenException;
import com.lilo.model.User;
import com.lilo.shared.WebSocketConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            try {
                String destination = accessor.getDestination();
                User authenticatedUser = tryAuthenticateUser(accessor);
                if (destination.startsWith("topic")) {
                    validateUserPartyRelationship(accessor, authenticatedUser);
                }
            } catch (Exception e) {
                log.warn("Subscription rejected for destination {}: {}", accessor.getDestination(), e.getMessage());
                throw e;
            }
        }

        // If not a SUBSCRIBE, or if validation passed, let the message proceed
        return message;
    }

    private User tryAuthenticateUser(StompHeaderAccessor stompHeaderAccessor) {
        Authentication authentication = (Authentication) stompHeaderAccessor.getUser();
        if (authentication == null) {
            throw new SubscriptionForbiddenException("User is unauthorized");
        }
        User authenticatedUser = (User) authentication.getPrincipal();
        return authenticatedUser;
//        String userPartyId = authenticatedUser.getPartyId();
//
//        if (userPartyId == null || !userPartyId.equals(requestedPartyId))
//            throw new SubscriptionForbiddenException("User is not a member of this party");
//
//        log.info("User {} successfully subscribed to {}", authenticatedUser.getId(), destination);

    }

    private void validateUserPartyRelationship(StompHeaderAccessor stompHeaderAccessor, User authenticatedUser) {
        String userPartyId = authenticatedUser.getPartyId();
        String requestedPartyId = tryGetRequestedPartyId(stompHeaderAccessor);

        if (userPartyId == null || !userPartyId.equals(requestedPartyId))
            throw new SubscriptionForbiddenException("User is not a member of this party");
    }

    private String tryGetRequestedPartyId(StompHeaderAccessor stompHeaderAccessor) {
        String subscriptionDestination = stompHeaderAccessor.getDestination();

        if (subscriptionDestination == null)
            throw new SubscriptionForbiddenException("Invalid destination");


        return parseRequestedPartyId(subscriptionDestination);
    }

    private String parseRequestedPartyId(String subscriptionDestination) {
        int partyIdStartIdx = subscriptionDestination.indexOf('.') + 1;
        if (subscriptionDestination.startsWith(WebSocketConstants.TOPIC_PARTY) && subscriptionDestination.length() >= partyIdStartIdx + WebSocketConstants.PARTY_ID_LENGTH) {
            return subscriptionDestination.substring(partyIdStartIdx, (partyIdStartIdx + WebSocketConstants.PARTY_ID_LENGTH));
        } else if (subscriptionDestination.startsWith(WebSocketConstants.TOPIC_PARTY_CHAT) && subscriptionDestination.length() >= partyIdStartIdx + WebSocketConstants.CHAT_ID_LENGTH) {
            return subscriptionDestination.substring(partyIdStartIdx, (partyIdStartIdx + WebSocketConstants.PARTY_ID_LENGTH));
        }
        throw new SubscriptionForbiddenException("Invalid destination");
    }

}