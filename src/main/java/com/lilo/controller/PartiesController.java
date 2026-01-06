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
import com.lilo.service.PartyMessageService;
import com.lilo.shared.WebConstants;
import com.lilo.shared.WebSocketConstants;
import com.lilo.shared.annotations.AllowedValues;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.lilo.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    private final PartyMessageService partyMessageService;

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
    @GetMapping
    public ResponseEntity<?> listParties(@RequestParam(name = "page", defaultValue = "0") int pageNumber,
                                        @RequestParam(name = "size", defaultValue = "10") int size,
                                        @RequestParam(name = "sortBy", defaultValue = "createdAt") @AllowedValues(values = {"createdAt"}) String sortBy) {

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        Page<PartySummaryDTO> storedParties = partiesService.getParties(pageNumber, size, Sort.by(Sort.Order.desc(sortBy)))
                .map(p-> PartySummaryDTO.fromParty(p, baseUrl));
        return ResponseEntity.ok(storedParties);
    }
    @PatchMapping("/join")
    public ResponseEntity<?> joinParty(@Valid @RequestBody JoinPartyRequestDTO joinPartyRequestDTO, BindingResult bindingResult, @AuthenticationPrincipal User authenticatedUser) {
        if (bindingResult.hasErrors())
            return buildBindingErrorResponse(bindingResult);

        if (authenticatedUser.getPartyId() != null && !authenticatedUser.getPartyId().equals(joinPartyRequestDTO.getPartyId()))
            return buildErrorResponse(HttpStatus.CONFLICT, "User is already in a party");

        TableOperationResult userJoiningResult = partiesService.joinParty(joinPartyRequestDTO.getPartyId(), authenticatedUser);
        if (userJoiningResult.isSuccess()) {
            PartyDetailsDTO partyDetails = partiesService.getPartyDetails(joinPartyRequestDTO.getPartyId());
            simpMessagingTemplate.convertAndSend(WebSocketConstants.TOPIC_PARTY_MEMBER_EVENTS, new PartyMemberEventOutputDTO(authenticatedUser.getId(), authenticatedUser.getName(), PartyMemberEvent.JOINED, Instant.now()));
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

    @GetMapping("/{partyId}/messages")
    public ResponseEntity<?> getPartyMessages(@PathVariable("partyId") String partyId,
                                              @RequestParam(name = "page", defaultValue = "0") int pageNumber,
                                              @RequestParam(name = "size", defaultValue = "6") int size,
                                              @AuthenticationPrincipal User authenticatedUser) {

        if (authenticatedUser.getPartyId() == null || !authenticatedUser.getPartyId().equals(partyId))
            return buildErrorResponse(HttpStatus.FORBIDDEN, "User is not a member of this party");

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String profilePicturesUrl = String.format("%s%s", baseUrl, WebConstants.profilePictureUrlPattern.replace("**", ""));

        Page<PartyMessageOutputDTO> partyMessages = partyMessageService.findByPartyId(partyId, pageNumber, size, Sort.by(Sort.Order.desc("createdAt")))
                .map(pm -> PartyMessageOutputDTO.fromPartyMessage(pm, String.format("%s%s", profilePicturesUrl, pm.getUser().getProfilePicture())));
        return buildSuccessResponse(partyMessages);
    }

    @PostMapping
    public ResponseEntity<?> createWatchParty(@RequestPart(required = false) MultipartFile thumbnailMultipartFile,  @RequestPart String partyName, @AuthenticationPrincipal User authenticatedUser) throws Exception {


        if (authenticatedUser.getPartyId() != null)
            return buildErrorResponse(HttpStatus.CONFLICT, "User is already in a party");

        Party newParty = new Party(authenticatedUser.getId(), partyName);
        TableOperationResult partySavingResult = partiesService.save(newParty, thumbnailMultipartFile);
        if (!partySavingResult.isSuccess())
            return buildErrorResponse(new ApiError(partySavingResult.getSuggestedStatusCode(), partySavingResult.getErrorMessage()));

        authenticatedUser.setPartyId(newParty.getId());
        authenticatedUser.setPartyJoinTime(Instant.now());
        userService.update(authenticatedUser);
        URI location = MvcUriComponentsBuilder
                .fromMethodCall(on(PartiesController.class).getPartyDetails(newParty.getId(), null))
                .build()
                .toUri();
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        return ResponseEntity.created(location).body(ApiResponse.withSuccess(PartySummaryDTO.fromParty(newParty, baseUrl)));
    }

    @DeleteMapping("/leave")
    @ResponseBody
    public ResponseEntity<?> leaveParty(@AuthenticationPrincipal User authenticatedUser) {
        String partyId = authenticatedUser.getPartyId();
        if (partyId == null)
            return ResponseEntity.noContent().build();

        partiesService.processUserLeave(authenticatedUser);
        simpMessagingTemplate.convertAndSend(WebSocketConstants.TOPIC_PARTY_MEMBER_EVENTS, new PartyMemberEventOutputDTO(authenticatedUser.getId(), authenticatedUser.getName(), PartyMemberEvent.LEFT, Instant.now()));
        return ResponseEntity.noContent().build();
    }

//    @EventListener(classes = SessionDisconnectEvent.class)
//    void sessionDisconnectEventHandler(SessionDisconnectEvent event) {
//        this.leaveParty((User) event.getUser());
//    }

}
