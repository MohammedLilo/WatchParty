package com.lilo.controller.websocket;

import com.lilo.model.User;
import com.lilo.model.dto.PartyMessageInputDTO;
import com.lilo.model.dto.PartyMessageOutputDTO;
import com.lilo.service.PartyMessageService;
import com.lilo.shared.WebConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.lilo.model.PartyMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.lilo.shared.WebSocketConstants.TOPIC_PARTY_CHAT;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatsWebsocketController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final PartyMessageService partyMessageService;
    @Value("app.host")
    private String host;
    /// TODO
    /// Add profile pictures for sender of the message

    @MessageMapping("/watchParty-chats/{id}")
//	@SendTo("/topic/chat.{id}")
//    @SendTo(TOPIC_PARTY_CHAT + ".{id}")
    private PartyMessageOutputDTO sendMessage(@Payload PartyMessageInputDTO partyMessageInputDTO, @DestinationVariable("id") String partyId, Authentication authentication) {
//		simpMessagingTemplate.convertAndSend(TOPIC_PARTY_CHAT + "." + partyId, chatMessageInputDTO);
        log.info(partyMessageInputDTO.toString());

        User authenticatedUser = (User) authentication.getPrincipal();
        PartyMessage newPartyMessage = new PartyMessage(partyMessageInputDTO.getContent(), authenticatedUser.getName(), partyId, authenticatedUser);

        String baseUrl = this.host;
        String senderProfilePictureUrl = String.format("%s%s%s", baseUrl, WebConstants.profilePictureUrlPattern.replace("**", ""), authenticatedUser.getProfilePicture());

        partyMessageService.save(newPartyMessage);
        simpMessagingTemplate.convertAndSend(TOPIC_PARTY_CHAT + "." + partyId, PartyMessageOutputDTO.fromPartyMessage(newPartyMessage, senderProfilePictureUrl));

        return PartyMessageOutputDTO.fromPartyMessage(newPartyMessage, senderProfilePictureUrl);
    }

}
