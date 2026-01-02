package com.lilo.service;

import com.lilo.model.DefaultThumbnail;
import com.lilo.model.Party;
import com.lilo.model.User;
import com.lilo.model.dto.PartyDetailsDTO;
import com.lilo.model.dto.PartyMemberDTO;
import com.lilo.operationResult.TableOperationResult;
import com.lilo.repository.PartiesRepository;
import com.lilo.shared.WebConstants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartiesService {
    private final PartiesRepository partiesRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final DefaultThumbnailService defaultThumbnailService;
//    private final UserRepository userRepository;

    public TableOperationResult save(Party party, MultipartFile multipartFile) throws Exception {
        if (userService.ownsParty(party.getOwnerUserId()))
            return TableOperationResult.fromFailure("User already owns a party", HttpStatus.CONFLICT.value());


        if(!multipartFile.isEmpty()) {
            String fileName = UUID.randomUUID().toString();
            party.setThumbnailFileName(fileName);
            fileStorageService.save(fileName, multipartFile);
        }
        else {
            DefaultThumbnail defaultThumbnail = defaultThumbnailService.findRandomly().orElseThrow(() -> new Exception("INTERNAL SERVER ERROR: default thumbnail was not found in DB"));
            party.setThumbnailFileName(defaultThumbnail.getThumbnailFileName());
        }
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

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String path = WebConstants.thumbnailsUrlPattern.replace("**", "");
        String thumbnailUrl = String.format("%s%s%s", baseUrl, path, storedParty.getThumbnailFileName());

        return new PartyDetailsDTO(partyId,
                storedParty.getName(),
                ownerUser.getId(),
                ownerUser.getName(),
                storedParty.getCurrentVideoUrl(),
                storedParty.getLatestSyncEventJson(),
                storedParty.isPrivate(),
                thumbnailUrl,
                memberDTOs);
    }

    @Transactional(readOnly = true)
    public Party findPartyById(String partyId) {
        return partiesRepository.findById(partyId)
                .orElseThrow(() -> new EntityNotFoundException("Party not found with id: " + partyId));
    }
    @Transactional(readOnly = true)
    public Page<Party> getParties(int pageNumber, int pageSize, Sort sort) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        return partiesRepository.findAll(pageable);
    }

    public void update(Party storedParty) {
        partiesRepository.save(storedParty);
    }
}
