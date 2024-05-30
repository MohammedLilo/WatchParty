package com.lilo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lilo.domain.User;
import com.lilo.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LoginController {
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@ModelAttribute User user) {
		user.setId(null);
		user.setEmail(user.getEmail().toLowerCase());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole("USER");
		userService.save(user);

		return ResponseEntity.status(HttpStatus.CREATED).body("Account Registered successfully.");
	}
	@GetMapping("/public")
	public String getMethodName() {
		return "hello";
	}
	

}
