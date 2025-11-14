package com.lilo.service;

import com.lilo.model.Party;
import com.lilo.model.User;
import com.lilo.model.dto.PartyDetailsDTO;
import com.lilo.model.dto.PartyMemberDTO;
import com.lilo.operationResult.TableOperationResult;
import com.lilo.repository.PartiesRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartiesService {
    private final PartiesRepository partiesRepository;
    private final UserService userService;
//    private final UserRepository userRepository;

    public TableOperationResult save(Party party) {
        if (userService.ownsParty(party.getOwnerUserId()))
            return TableOperationResult.fromFailure("User already owns a party", HttpStatus.CONFLICT.value());

        partiesRepository.save(party);
        return TableOperationResult.fromSuccess();
    }

    @Transactional
    public void processUserLeave(User user) {
        String partyId = user.getPartyId();
        if (partyId == null || partyId.isEmpty()) {
            log.warn("User {} attempted to leave but was not in a party.", user.getId());
            return;
        }

        Party storedParty = partiesRepository.findById(partyId).orElse(null);

        user.setPartyId(null);
        user.setPartyJoinTime(null);
        user.setParty(null);
        userService.update(user);

        boolean isUserOwner = user.getId().equals(storedParty.getOwnerUserId());
        if (isUserOwner)
            tryTransferOwnerShip(storedParty);
        else
            log.info("Regular member left Party {}. Ownership retained by {}.", partyId, storedParty.getOwnerUserId());
    }

    private void tryTransferOwnerShip(Party storedParty) {
        String partyId = storedParty.getId();
        User nextCandidatePartyOwner = userService.findFirstByPartyIdOrderByJoinTime(partyId).orElse(null);

        if (nextCandidatePartyOwner == null) {
            partiesRepository.deleteById(partyId);
            log.info("Deleting party {}  because no members remained after owner left.", partyId);
        } else {
            storedParty.setOwnerUserId(nextCandidatePartyOwner.getId());
            partiesRepository.save(storedParty);
            log.info("Party {} ownership transferred to {}.", partyId, nextCandidatePartyOwner.getId());
        }
    }

    public TableOperationResult joinParty(String partyId, User authenticatedUser) {
        Party storedParty = partiesRepository.findById(partyId).orElse(null);

        if (storedParty == null)
            return TableOperationResult.fromFailure("Party not found", HttpStatus.NOT_FOUND.value());

        if (!storedParty.isPrivate()) {
            authenticatedUser.setPartyId(storedParty.getId());
            authenticatedUser.setPartyJoinTime(Instant.now());
            return userService.update(authenticatedUser);
        } else {
            return TableOperationResult.fromFailure("Party is private", HttpStatus.FORBIDDEN.value());
        }
    }

    @Transactional(readOnly = true)
    public PartyDetailsDTO getPartyDetails(String partyId) {
        Party storedParty = partiesRepository.findById(partyId)
                .orElseThrow(() -> new EntityNotFoundException("Party not found with id: " + partyId));

        List<User> members = userService.findAllByPartyIdOrderByJoinTime(partyId);

        User ownerUser = members.stream()
                .filter(user -> user.getId().equals(storedParty.getOwnerUserId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Party owner not found among members"));

        List<PartyMemberDTO> memberDTOs = members.stream()
                .map(user -> new PartyMemberDTO(user.getId(), user.getName(), user.getPartyJoinTime()))
                .toList();

        return new PartyDetailsDTO(partyId,
                storedParty.getName(),
                ownerUser.getId(),
                ownerUser.getName(),
                storedParty.getCurrentVideoUrl(),
                storedParty.getLatestSyncEventJson(),
                memberDTOs);
    }

    @Transactional(readOnly = true)
    public Party findPartyById(String partyId) {
        return partiesRepository.findById(partyId)
                .orElseThrow(() -> new EntityNotFoundException("Party not found with id: " + partyId));
    }

    public void update(Party storedParty) {
        partiesRepository.save(storedParty);
    }
}
