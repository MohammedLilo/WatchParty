package com.lilo.model.dto;

import com.lilo.model.ChatMessage;

public class ChatMessageOutputDTO {
    private String id;
    private String partyId;
    private long userId;
    private String content;
    private String senderName;

    private ChatMessageOutputDTO(ChatMessage chatMessage) {
        id = chatMessage.getId();
        partyId = chatMessage.getPartyId();
        userId = chatMessage.getUser().getId();
        content = chatMessage.getContent();
        senderName = chatMessage.getSenderName();
    }

    public static ChatMessageOutputDTO fromChatMessage(ChatMessage chatMessage) {
        return new ChatMessageOutputDTO(chatMessage);
    }
}
