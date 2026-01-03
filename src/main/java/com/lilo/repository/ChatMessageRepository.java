package com.lilo.repository;

import com.lilo.model.PartyMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<PartyMessage, String> {
}
