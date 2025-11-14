package com.lilo.controller;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.lilo.enums.PartyMemberEvent;
import com.lilo.model.*;
import com.lilo.model.dto.*;
import com.lilo.operationResult.TableOperationResult;
import com.lilo.service.PartiesService;
import com.lilo.shared.WebSocketConstants;
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
        if (userJoiningResult.isSuccess()) {
            PartyDetailsDTO partyDetails = partiesService.getPartyDetails(joinPartyRequestDTO.getPartyId());
            simpMessagingTemplate.convertAndSend(WebSocketConstants.TOPIC_PARTY_MEMBER_EVENTS, new PartyMemberEventOutputDTO(authenticatedUser.getId(), PartyMemberEvent.JOINED, Instant.now()));
            return buildSuccessResponse(partyDetails);
        }
        return buildErrorResponse(userJoiningResult);
    }

    @GetMapping("{partyId}")
    public ResponseEntity<?> getPartyDetails(@PathVariable("partyId") String partyId, @AuthenticationPrincipal User authenticatedUser) {

        if (authenticatedUser.getPartyId() == null || !authenticatedUser.getPartyId().equals(partyId))
            return buildErrorResponse(HttpStatus.FORBIDDEN, "User is not a member of this party");

        PartyDetailsDTO partyDetails = partiesService.getPartyDetails(partyId);
//        return ResponseEntity.ok(partyDetails);
        return buildSuccessResponse(partyDetails);
    }



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

    @DeleteMapping("/leave")
    @ResponseBody
    public ResponseEntity<?> leaveParty(@AuthenticationPrincipal User authenticatedUser) {
        String partyId = authenticatedUser.getPartyId();
        if (partyId == null)
            return ResponseEntity.noContent().build();

        partiesService.processUserLeave(authenticatedUser);
        simpMessagingTemplate.convertAndSend(WebSocketConstants.TOPIC_PARTY_MEMBER_EVENTS, new PartyMemberEventOutputDTO(authenticatedUser.getId(), PartyMemberEvent.LEFT, Instant.now()));
        return ResponseEntity.noContent().build();
    }

//    @EventListener(classes = SessionDisconnectEvent.class)
//    void sessionDisconnectEventHandler(SessionDisconnectEvent event) {
//        this.leaveParty((User) event.getUser());
//    }

}
