package com.lilo.repository;

import com.lilo.model.PartyMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<PartyMessage, String> {
    Page<PartyMessage> findByPartyId(String partyId, Pageable pageable);
}
