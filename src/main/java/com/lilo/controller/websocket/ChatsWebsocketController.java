package com.lilo.controller.websocket;

import com.lilo.model.User;
import com.lilo.model.dto.ChatMessageInputDTO;
import com.lilo.model.dto.ChatMessageOutputDTO;
import com.lilo.service.ChatMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.lilo.model.ChatMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.lilo.shared.WebSocketConstants.TOPIC_PARTY_CHAT;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatsWebsocketController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatMessageService chatMessageService;
/// TODO
/// implement an endpoint to retrieve party messages
/// and fix the messaging
    @MessageMapping("/watchParty-chats/{id}")
//	@SendTo("/topic/chat.{id}")
//    @SendTo(TOPIC_PARTY_CHAT + ".{id}")
    private ChatMessageOutputDTO sendMessage(@Payload ChatMessageInputDTO chatMessageInputDTO, @DestinationVariable("id") String partyId, Authentication authentication) {
//		simpMessagingTemplate.convertAndSend(TOPIC_PARTY_CHAT + "." + partyId, chatMessageInputDTO);
        log.info(chatMessageInputDTO.toString());
        User authenticatedUser = (User) authentication.getPrincipal();
        ChatMessage newChatMessage = new ChatMessage(chatMessageInputDTO.getContent(), authenticatedUser.getName(), partyId, authenticatedUser);
        chatMessageService.save(newChatMessage);
        simpMessagingTemplate.convertAndSend(TOPIC_PARTY_CHAT + "." + partyId, ChatMessageOutputDTO.fromChatMessage(newChatMessage));

        return ChatMessageOutputDTO.fromChatMessage(newChatMessage);
    }

}
