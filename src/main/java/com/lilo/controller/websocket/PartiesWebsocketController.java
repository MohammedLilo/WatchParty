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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PartiesWebsocketController {
    private final PartiesService partiesService;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/party/{id}")
//    @SendTo("/topic/party.{id}")
    @SendTo(WebSocketConstants.TOPIC_PARTY + ".{id}")
    PartySyncEventOutputDTO handleSync(@Payload PartySyncEventInputDTO partySyncEventInputDTO, @DestinationVariable("id") String id, Authentication authentication) throws JsonProcessingException {

        User authenticatedUser = (User) authentication.getPrincipal();
        Party storedParty = partiesService.findPartyById(id);

        if(! authenticatedUser.getId().equals(storedParty.getOwnerUserId())) {
            log.info("User {} is not the owner of the party. Ignoring the {} event", authenticatedUser.getId(), partySyncEventInputDTO.getEvent());
            simpMessagingTemplate.convertAndSendToUser(authenticatedUser.getEmail(), WebSocketConstants.QUEUE_ERRORS,"You are not the owner of this party.");
            return null;
        }
        PartySyncEventOutputDTO newPartySyncEventOutputDTO;
        PartySyncEventOutputDTO previousPartySyncEventOutputDTO = (storedParty.getLatestSyncEventJsonPayload() != null) ? objectMapper.readValue(storedParty.getLatestSyncEventJsonPayload(), PartySyncEventOutputDTO.class) : null;
        PartySyncEventOutputDTO earliestPartySyncEventOutputDTO = (previousPartySyncEventOutputDTO != null && previousPartySyncEventOutputDTO.getPreviousSyncEventJsonPayload() != null) ? previousPartySyncEventOutputDTO.getPreviousSyncEventJsonPayload() : null;

        if (previousPartySyncEventOutputDTO != null && partySyncEventInputDTO.getEvent().equals(previousPartySyncEventOutputDTO.getEvent())) {
            newPartySyncEventOutputDTO = new PartySyncEventOutputDTO(authenticatedUser.getId(),
                    authenticatedUser.getName(),
                    partySyncEventInputDTO.getEvent(),
                    partySyncEventInputDTO.getVideoUrl(),
                    partySyncEventInputDTO.getVideoCurrentTime(),
                    partySyncEventInputDTO.getEventDateTime(),
                    earliestPartySyncEventOutputDTO);
        } else {
            newPartySyncEventOutputDTO = new PartySyncEventOutputDTO(authenticatedUser.getId(),
                    authenticatedUser.getName(),
                    partySyncEventInputDTO.getEvent(),
                    partySyncEventInputDTO.getVideoUrl(),
                    partySyncEventInputDTO.getVideoCurrentTime(),
                    partySyncEventInputDTO.getEventDateTime(),
                    previousPartySyncEventOutputDTO);
        }

        if (newPartySyncEventOutputDTO.getPreviousSyncEventJsonPayload() != null && newPartySyncEventOutputDTO.getPreviousSyncEventJsonPayload().getPreviousSyncEventJsonPayload() != null)
            newPartySyncEventOutputDTO.getPreviousSyncEventJsonPayload().setPreviousSyncEventJsonPayload(null);



        String newPartySyncEventJSON = objectMapper.writeValueAsString(newPartySyncEventOutputDTO);
        String newPreviousPartySyncEventJSON = (newPartySyncEventOutputDTO.getPreviousSyncEventJsonPayload() != null)
                                                                                                        ? objectMapper.writeValueAsString(newPartySyncEventOutputDTO.getPreviousSyncEventJsonPayload())
                                                                                                        : null;
        storedParty.setLatestSyncEventJsonPayload(newPartySyncEventJSON);

        if (partySyncEventInputDTO.getEvent() == PartyVideoEvent.CHANGE_URL)
            storedParty.setCurrentVideoUrl(partySyncEventInputDTO.getVideoUrl());

        partiesService.update(storedParty);

        return newPartySyncEventOutputDTO;
    }


}
