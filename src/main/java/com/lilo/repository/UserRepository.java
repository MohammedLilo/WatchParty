package com.lilo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lilo.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
	User findByEmail(String email);
}
