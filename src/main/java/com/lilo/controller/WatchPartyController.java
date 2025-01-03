package com.lilo.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.lilo.model.PartyDetailTuple;
import com.lilo.model.PartySyncMessage;
import com.lilo.model.SyncNewUserMessage;
import com.lilo.model.User;
import com.lilo.service.UserService;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WatchPartyController {
	private final UserService userService;
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final Map<String, PartyDetailTuple> partyDetailTupleMap = new HashMap<>();

	@PreDestroy
	void deleteAllPartiesFromDatabase() {
		userService.nullifyPartyIdForAllUsers();
	}
	@GetMapping("/watch-party")
	String getWatchPartyPage(@RequestParam(name = "src", required = false) String src) {
		return "/party-page.html?src=" + src;
	}

	/*
	 * @GetMapping("/watch-parties/{party-id}/members-count") public SseEmitter
	 * getMethodName(@PathVariable("party-id") String partyId) { SseEmitter emitter
	 * = new SseEmitter(5000L); try {
	 * emitter.send(SseEmitter.event().name("PartyMembersCount")
	 * .data(this.partyDetailTupleMap.get(partyId).getMembersCount())); } catch
	 * (IOException e) { log.error("an IOException occured.. " + e.getMessage()); }
	 * return emitter; }
	 */
	@PutMapping("/watch-parties/{id}")
	@ResponseBody
	public ResponseEntity<?> joinParty(@PathVariable("id") String partyId, Principal principal) {
		User user = userService.findByEmail(principal.getName());
		if (user == null)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

		if (user.getPartyId() == null || user.getPartyId().isEmpty() || !user.getPartyId().equals(partyId)) {
			user.setPartyId(partyId);
			userService.update(user);
			simpMessagingTemplate.convertAndSend("/topic/watch-party." + partyId,
					new PartySyncMessage(user.getId(), user.getName(), "join", null, null, System.currentTimeMillis()));
			PartyDetailTuple tuple = partyDetailTupleMap.get(partyId);
			tuple.incrementMembersCount();
			new Thread(() -> {
				try {
					TimeUnit.MILLISECONDS.sleep(1000);
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
				simpMessagingTemplate.convertAndSend("/topic/watch-party-members-count." + partyId,
						tuple.getMembersCount());
			}).start();
			return ResponseEntity.status(HttpStatus.OK).body(calculateSyncInfo(partyId));
		}
	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user is already in a party");
	}

	private SyncNewUserMessage calculateSyncInfo(String partyId) {
		PartySyncMessage latestSyncMessage = partyDetailTupleMap.get(partyId).getLatestPartySyncMessage();
		SyncNewUserMessage syncNewUserMessage = new SyncNewUserMessage();
		String event = latestSyncMessage.getEvent();
		PartyDetailTuple tuple = this.partyDetailTupleMap.get(partyId);
		switch (event) {
		case "play":
			syncNewUserMessage.setEvent("play");
			syncNewUserMessage.setVideoCurrentTime(latestSyncMessage.getVideoCurrentTime());
			syncNewUserMessage.setEventDateTime(latestSyncMessage.getEventDateTime());
			syncNewUserMessage.setVideoUrl(latestSyncMessage.getVideoUrl());

			break;
		case "pause":
			syncNewUserMessage.setEvent("pause");
			syncNewUserMessage.setVideoCurrentTime(latestSyncMessage.getVideoCurrentTime());
			syncNewUserMessage.setVideoUrl(latestSyncMessage.getVideoUrl());

			break;
		case "seeked":
			syncNewUserMessage.setEvent("seeked");
			syncNewUserMessage.setPreviousEvent(tuple.getPreviousPartySyncMessage().getEvent());
			syncNewUserMessage.setVideoCurrentTime(latestSyncMessage.getVideoCurrentTime());
			syncNewUserMessage.setEventDateTime(latestSyncMessage.getEventDateTime());
			syncNewUserMessage.setVideoUrl(latestSyncMessage.getVideoUrl());
			break;
		case "url":
			syncNewUserMessage.setEvent("url");
			syncNewUserMessage.setEventDateTime(latestSyncMessage.getEventDateTime());
			syncNewUserMessage.setVideoUrl(latestSyncMessage.getVideoUrl());
			break;
		default:
			throw new RuntimeException("unexpected event happened!");
		}

		return syncNewUserMessage;
	}

	@PostMapping("/watch-parties")
	@ResponseBody
	public ResponseEntity<?> createWatchParty(Principal principal) {
		User user = userService.findByEmail(principal.getName());
		if (user == null)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

		if (user.getPartyId() == null || user.getPartyId().isEmpty()) {
			user.setPartyId(UUID.randomUUID().toString());
			userService.update(user);
			partyDetailTupleMap.put(user.getPartyId(), new PartyDetailTuple());
			String partyId = user.getPartyId();
			new Thread(() -> {
				try {
					TimeUnit.MILLISECONDS.sleep(1000);
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
				simpMessagingTemplate.convertAndSend("/topic/watch-party-members-count." + partyId,
						partyDetailTupleMap.get(partyId).getMembersCount());
			}).start();
		
		}
		return ResponseEntity.ok(user.getPartyId());
	}

	@MessageMapping("/watch-parties/{id}")
	@SendTo("/topic/watch-party.{id}")
	PartySyncMessage handleSync(@Payload PartySyncMessage partySyncMessage, @DestinationVariable("id") String id) {
		if (!partySyncMessage.getEvent().equals("join") && !partySyncMessage.getEvent().equals("left")) {
			PartyDetailTuple tuple = partyDetailTupleMap.get(id);
			tuple.setPreviousPartySyncMessage(tuple.getLatestPartySyncMessage());
			tuple.setLatestPartySyncMessage(partySyncMessage);
		}
		return partySyncMessage;
	}

	@DeleteMapping("/watch-parties")
	@ResponseBody
	ResponseEntity<Void> leaveParty(Principal principal) {
		User user = userService.findByEmail(principal.getName());

		if (user == null)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		if (user.getPartyId() == null || user.getPartyId().equals(""))
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

		String partyId = user.getPartyId();
		user.setPartyId(null);
		userService.update(user);

		partyDetailTupleMap.get(partyId).decrementMembersCount();

		PartyDetailTuple tuple = partyDetailTupleMap.get(partyId);
		PartySyncMessage partySyncMessage = new PartySyncMessage(user.getId(), user.getName(), "left", null, null,
				System.currentTimeMillis());
		new Thread(() -> {
			simpMessagingTemplate.convertAndSend("/topic/watch-party-members-count." + partyId,
					tuple.getMembersCount());
		}).start();

		// notify other party members that a user (name) left the party
		simpMessagingTemplate.convertAndSend("/topic/watch-party." + partyId, partySyncMessage);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@EventListener(classes = SessionDisconnectEvent.class)
	void sessionDisconnectEventHandler(SessionDisconnectEvent event) {
		this.leaveParty(event.getUser());
	}

}
