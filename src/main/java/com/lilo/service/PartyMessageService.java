package com.lilo.service;

import com.lilo.model.PartyMessage;
import com.lilo.operationResult.TableOperationResult;
import com.lilo.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartyMessageService {
    private final ChatMessageRepository chatMessageRepository;

    public TableOperationResult save(PartyMessage partyMessage) {
        chatMessageRepository.save(partyMessage);
        return TableOperationResult.fromSuccess();
    }

    @Transactional(readOnly = true)
    public Page<PartyMessage> findByPartyId(String partyId, int pageNumber, int pageSize, Sort sort) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        return chatMessageRepository.findByPartyId(partyId, pageable);
    }
}
