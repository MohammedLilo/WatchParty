package com.lilo.service;

import com.lilo.model.ChatMessage;
import com.lilo.operationResult.TableOperationResult;
import com.lilo.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    public TableOperationResult save(ChatMessage chatMessage){
        chatMessageRepository.save(chatMessage);
        return TableOperationResult.fromSuccess();
    }
    public Page<ChatMessage> findByPartyId(String partyId, int pageNumber, int pageSize, Sort sort) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        return chatMessageRepository.findAll(pageable);
    }
}
