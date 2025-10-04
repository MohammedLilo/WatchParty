package com.lilo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.lilo.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	boolean existsByEmailOrPhoneNumber(String email, String phoneNumber);
    boolean existsByIdAndPartyIdIsNotNull(long id);

	@Modifying
	@Query("UPDATE User u SET u.partyId= NULL")
	void nullifyPartyIdForAllUsers();

    int countByPartyId(String partyId);

    Optional<User> findFirstByPartyIdOrderByPartyJoinTimeAsc(String partyId);

    List<User> findAllByPartyIdOrderByPartyJoinTime(String partyId);
}
