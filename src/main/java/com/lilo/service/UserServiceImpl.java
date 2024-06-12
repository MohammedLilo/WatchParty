package com.lilo.service;

import org.springframework.stereotype.Service;

import com.lilo.domain.User;
import com.lilo.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;

	@Override
	public User findById(long id) {
		return userRepository.findById(id).get();
	}

	@Override
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public void save(User user) {
		if (userRepository.existsByEmail(user.getEmail()))
			throw new RuntimeException("this email is already taken");
		userRepository.save(user);
	}

	@Override
	public void update(User user) {
		userRepository.save(user);
	}

	@Transactional
	public void nullifyPartyIdForAllUsers() {
		userRepository.nullifyPartyIdForAllUsers();
	}

	@Override
	public void deleteById(long id) {
		userRepository.deleteById(id);
	}

}
