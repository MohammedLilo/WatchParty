package com.lilo.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.lilo.model.ChatMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {
//	private final SimpMessagingTemplate simpMessagingTemplate;

	@MessageMapping("/watch-party-chats/{id}")
	@SendTo("/topic/chat.{id}")
	private ChatMessage sendMessage(@Payload ChatMessage chatMessage, @DestinationVariable("id") String id) {
//		simpMessagingTemplate.convertAndSend("/topic/chat." + id, chatMessage);
		log.info(chatMessage.toString());
		return chatMessage;
	}

}
