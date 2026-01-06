package com.lilo.model.dto;

import com.lilo.model.PartyMessage;
import lombok.Data;

import java.time.Instant;

@Data
public class PartyMessageOutputDTO {
    private String id;
    private String partyId;
    private long userId;
    private String content;
    private String senderName;
    private String profilePictureUrl;
    private Instant createdAt;

    private PartyMessageOutputDTO(PartyMessage partyMessage, String profilePictureUrl) {
        id = partyMessage.getId();
        partyId = partyMessage.getPartyId();
        userId = partyMessage.getUser().getId();
        content = partyMessage.getContent();
        senderName = partyMessage.getSenderName();
        createdAt = partyMessage.getCreatedAt();
        this.profilePictureUrl = profilePictureUrl;
    }

    public static PartyMessageOutputDTO fromPartyMessage(PartyMessage partyMessage, String profilePictureUrl) {
        return new PartyMessageOutputDTO(partyMessage,  profilePictureUrl);
    }
}
