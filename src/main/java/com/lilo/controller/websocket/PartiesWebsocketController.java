package com.lilo.controller.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilo.enums.PartyVideoEvent;
import com.lilo.model.Party;
import com.lilo.model.User;
import com.lilo.model.dto.PartySyncEventInputDTO;
import com.lilo.model.dto.PartySyncEventOutputDTO;
import com.lilo.service.PartiesService;
import com.lilo.shared.WebSocketConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PartiesWebsocketController {
    private final PartiesService partiesService;
    private final ObjectMapper objectMapper;

    @MessageMapping("/party/{id}")
//    @SendTo("/topic/party.{id}")
    @SendTo(WebSocketConstants.TOPIC_PARTY + ".{id}")
    PartySyncEventOutputDTO handleSync(@Payload PartySyncEventInputDTO partySyncEventInputDTO, @DestinationVariable("id") String id, Authentication authentication) throws JsonProcessingException {

        User authenticatedUser = (User) authentication.getPrincipal();
        PartySyncEventOutputDTO partySyncEventOutputDTO = new PartySyncEventOutputDTO(authenticatedUser.getId(),
                                                                                        authenticatedUser.getName(),
                                                                                        partySyncEventInputDTO.getEvent(),
                                                                                        partySyncEventInputDTO.getVideoUrl(),
                                                                                        partySyncEventInputDTO.getVideoCurrentTime(),
                                                                                        partySyncEventInputDTO.getEventDateTime());

        Party storedParty = partiesService.findPartyById(id);

        String partySyncEventJSON = objectMapper.writeValueAsString(partySyncEventOutputDTO);
        storedParty.setLatestSyncEventJson(partySyncEventJSON);
        if (partySyncEventInputDTO.getEvent() == PartyVideoEvent.CHANGE_URL)
            storedParty.setCurrentVideoUrl(partySyncEventInputDTO.getVideoUrl());

        partiesService.update(storedParty);
        return partySyncEventOutputDTO;
    }



}
