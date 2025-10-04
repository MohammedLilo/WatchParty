package com.lilo.controller;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.lilo.enums.PartyEvent;
import com.lilo.model.*;
import com.lilo.model.dto.*;
import com.lilo.operationResult.TableOperationResult;
import com.lilo.service.PartiesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.lilo.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RequestMapping("/api/v1/parties")
@RestController
@RequiredArgsConstructor
@Slf4j
public class PartiesController extends BaseController {
    private final PartiesService partiesService;
    private final UserService userService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Map<String, PartyDetailTuple> partyDetailTupleMap = new HashMap<>();

//	@PreDestroy
//	void deleteAllPartiesFromDatabase() {
//		userService.nullifyPartyIdForAllUsers();
//	}
//	@GetMapping("/watch-party")
//	String getWatchPartyPage(@RequestParam(name = "src", required = false) String src) {
//		return "/party-page.html?src=" + src;
//	}

    /*
     * @GetMapping("/watch-parties/{party-id}/members-count") public SseEmitter
     * getMethodName(@PathVariable("party-id") String partyId) { SseEmitter emitter
     * = new SseEmitter(5000L); try {
     * emitter.send(SseEmitter.event().name("PartyMembersCount")
     * .data(this.partyDetailTupleMap.get(partyId).getMembersCount())); } catch
     * (IOException e) { log.error("an IOException occured.. " + e.getMessage()); }
     * return emitter; }
     */
    @PatchMapping("/join")
    public ResponseEntity<?> joinParty(@Valid @RequestBody JoinPartyRequestDTO joinPartyRequestDTO, BindingResult bindingResult, @AuthenticationPrincipal User authenticatedUser) {
        if (bindingResult.hasErrors())
            return buildBindingErrorResponse(bindingResult);

        if (authenticatedUser.getPartyId() != null && !authenticatedUser.getPartyId().equals(joinPartyRequestDTO.getPartyId()))
            return buildErrorResponse(HttpStatus.CONFLICT, "User is already in a party");

        TableOperationResult userJoiningResult = partiesService.joinParty(joinPartyRequestDTO.getPartyId(), authenticatedUser);
        if (!userJoiningResult.isSuccess())
            return buildErrorResponse(userJoiningResult);
        else {
            PartyDetailsDTO partyDetails = partiesService.getPartyDetails(joinPartyRequestDTO.getPartyId());
            return buildSuccessResponse(partyDetails);
        }
    }
    //            simpMessagingTemplate.convertAndSend("/topic/watch-party." + partyId,
//                    new PartySyncMessage(authenticatedUser.getId(), authenticatedUser.getName(), "join", null, null, System.currentTimeMillis()));
//            PartyDetailTuple tuple = partyDetailTupleMap.get(partyId);
//            tuple.incrementMembersCount();
//            new Thread(() -> {
//                try {
//                    TimeUnit.MILLISECONDS.sleep(1000);
//                } catch (InterruptedException e) {
//                    log.error(e.getMessage());
//                }
//                simpMessagingTemplate.convertAndSend("/topic/watch-party-members-count." + partyId,
//                        tuple.getMembersCount());
//            }).start();
//            return ResponseEntity.status(HttpStatus.OK).body(calculateSyncInfo(partyId));
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user is already in a party");
//    }
    @GetMapping("{partyId}")
    public ResponseEntity<?> getPartyDetails(@PathVariable("partyId") String partyId, @AuthenticationPrincipal User authenticatedUser) {

        if (authenticatedUser.getPartyId() == null || !authenticatedUser.getPartyId().equals(partyId))
            return buildErrorResponse(HttpStatus.FORBIDDEN, "User is not a member of this party");

        PartyDetailsDTO partyDetails = partiesService.getPartyDetails(partyId);
        return ResponseEntity.ok(partyDetails);
    }

//    private SyncNewUserMessage calculateSyncInfo(String partyId) {
//        PartySyncEventInputDTO latestSyncMessage = partyDetailTupleMap.get(partyId).getLatestPartySyncEventInputDTO();
//        SyncNewUserMessage syncNewUserMessage = new SyncNewUserMessage();
//        PartyEvent event = latestSyncMessage.getEvent();
//        PartyDetailTuple tuple = this.partyDetailTupleMap.get(partyId);
//        switch (event) {
//            case PLAY:
//                syncNewUserMessage.setEvent("play");
//                syncNewUserMessage.setVideoCurrentTime(latestSyncMessage.getVideoCurrentTime());
//                syncNewUserMessage.setEventDateTime(latestSyncMessage.getEventDateTime());
//                syncNewUserMessage.setVideoUrl(latestSyncMessage.getVideoUrl());
//
//                break;
//            case PAUSE:
//                syncNewUserMessage.setEvent("pause");
//                syncNewUserMessage.setVideoCurrentTime(latestSyncMessage.getVideoCurrentTime());
//                syncNewUserMessage.setVideoUrl(latestSyncMessage.getVideoUrl());
//
//                break;
//            case SEEK:
//                syncNewUserMessage.setEvent("seeked");
//                syncNewUserMessage.setPreviousEvent(tuple.getPreviousPartySyncEventInputDTO().getEvent());
//                syncNewUserMessage.setVideoCurrentTime(latestSyncMessage.getVideoCurrentTime());
//                syncNewUserMessage.setEventDateTime(latestSyncMessage.getEventDateTime());
//                syncNewUserMessage.setVideoUrl(latestSyncMessage.getVideoUrl());
//                break;
//            case CHANGE_URL:
//                syncNewUserMessage.setEvent("url");
//                syncNewUserMessage.setEventDateTime(latestSyncMessage.getEventDateTime());
//                syncNewUserMessage.setVideoUrl(latestSyncMessage.getVideoUrl());
//                break;
//            default:
//                throw new RuntimeException("unexpected event happened!");
//        }
//
//        return syncNewUserMessage;
//    }

    @PostMapping
    public ResponseEntity<?> createWatchParty(@Valid @RequestBody PartyInputDTO partyInputDTO, BindingResult bindingResult, @AuthenticationPrincipal User authenticatedUser) {
        if (bindingResult.hasErrors())
            return buildBindingErrorResponse(bindingResult);

        if (authenticatedUser.getPartyId() != null)
            return buildErrorResponse(HttpStatus.CONFLICT, "User is already in a party");

        Party newParty = new Party(authenticatedUser.getId(), partyInputDTO.getPartyName());
        TableOperationResult partySavingResult = partiesService.save(newParty);
        if (!partySavingResult.isSuccess())
            return buildErrorResponse(new ApiError(partySavingResult.getSuggestedStatusCode(), partySavingResult.getErrorMessage()));

        authenticatedUser.setPartyId(newParty.getId());
        authenticatedUser.setPartyJoinTime(Instant.now());
        userService.update(authenticatedUser);
        URI location = MvcUriComponentsBuilder
                .fromMethodCall(on(PartiesController.class).getPartyDetails(newParty.getId(), null))
                .build()
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.withSuccess(newParty));
    }
//            partyDetailTupleMap.put(authenticatedUser.getPartyId(), new PartyDetailTuple());
//            String partyId = authenticatedUser.getPartyId();
//            new Thread(() -> {
//                try {
//                    TimeUnit.MILLISECONDS.sleep(1000);
//                } catch (InterruptedException e) {
//                    log.error(e.getMessage());
//                }
//                simpMessagingTemplate.convertAndSend("/topic/watch-party-members-count." + partyId,
//                        partyDetailTupleMap.get(partyId).getMembersCount());
//            }).start();

//        return buildSuccessResponse(HttpStatus.CREATED, newParty);
//    }

//    @MessageMapping("/watch-parties/{id}")
//    @SendTo("/topic/watch-party.{id}")
//    PartySyncMessage handleSync(@Payload PartySyncMessage partySyncMessage, @DestinationVariable("id") String id) {
//        if (!partySyncMessage.getEvent().equals("join") && !partySyncMessage.getEvent().equals("left")) {
//            PartyDetailTuple tuple = partyDetailTupleMap.get(id);
//            tuple.setPreviousPartySyncMessage(tuple.getLatestPartySyncMessage());
//            tuple.setLatestPartySyncMessage(partySyncMessage);
//        }
//        return partySyncMessage;
//    }

    @DeleteMapping("/leave")
    @ResponseBody
    public ResponseEntity<?> leaveParty(@AuthenticationPrincipal User authenticatedUser) {
        String partyId = authenticatedUser.getPartyId();
        if (partyId == null)
            return ResponseEntity.noContent().build();

        partiesService.processUserLeave(authenticatedUser);
        return ResponseEntity.noContent().build();
//        partyDetailTupleMap.get(partyId).decrementMembersCount();
//        PartyDetailTuple tuple = partyDetailTupleMap.get(partyId);
//        PartySyncMessage partySyncMessage = new PartySyncMessage(user.getId(), user.getName(), "left", null, null,
//                System.currentTimeMillis());
//        new Thread(() -> {
//            simpMessagingTemplate.convertAndSend("/topic/watch-party-members-count." + partyId,
//                    tuple.getMembersCount());
//        }).start();
//
//        // notify other party members that a user (name) left the party
//        simpMessagingTemplate.convertAndSend("/topic/watch-party." + partyId, partySyncMessage);
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

//    @EventListener(classes = SessionDisconnectEvent.class)
//    void sessionDisconnectEventHandler(SessionDisconnectEvent event) {
//        this.leaveParty((User) event.getUser());
//    }

}
