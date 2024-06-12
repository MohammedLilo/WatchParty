package com.lilo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.lilo.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
	User findByEmail(String email);

	boolean existsByEmail(String email);

	@Modifying
	@Query("UPDATE User u SET u.partyId= NULL")
	void nullifyPartyIdForAllUsers();
}
