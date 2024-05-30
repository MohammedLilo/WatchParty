package com.lilo.controller;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lilo.domain.PartySynchMessage;
import com.lilo.domain.SyncNewUserMessage;
import com.lilo.domain.User;
import com.lilo.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WatchPartyController {
	private final UserService userService;
	private final SimpMessagingTemplate simpMessagingTemplate;

	@PostMapping("/watch-parties")
	@ResponseBody
	public String createWatchParty(Authentication authentication) {
		User user = userService.findByEmail(authentication.getName());
		if (user == null)
			throw new RuntimeException("Error finding user for email : " + authentication.getName());

		if (user.getPartyId() == null || user.getPartyId().isEmpty()) {
			user.setPartyId(UUID.randomUUID().toString());
			userService.save(user);
		}
		return user.getPartyId();
	}

	@GetMapping("/watch-parties/{id}")
	@ResponseBody
	public void joinParty(@PathVariable("id") String id, Authentication authentication) {
		User user = userService.findByEmail(authentication.getName());
		if (user == null)
			throw new RuntimeException("Error finding user for email : " + authentication.getName());

		if (user.getPartyId() == null || user.getPartyId().isEmpty() || !user.getPartyId().equals(id)) {
			user.setPartyId(id);
			userService.save(user);
		}
		simpMessagingTemplate.convertAndSend("/topic/watch-party." + id, new SyncNewUserMessage(user.getName()));
	}

	@MessageMapping("/watch-parties/{id}")
	@SendTo("/topic/watch-party.{id}")
	PartySynchMessage handleSynch(@Payload PartySynchMessage partySynchMessage, @DestinationVariable("id") String id) {
		return partySynchMessage;
	}

	@DeleteMapping("/watch-parties")
	void leaveParty(Authentication authentication) {
		User user = userService.findByEmail(authentication.getName());
		if (user == null)
			throw new RuntimeException("Error finding user for email : " + authentication.getName());
		user.setPartyId(null);
		userService.save(user);
	}

}
