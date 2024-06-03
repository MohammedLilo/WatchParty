package com.lilo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.lilo.domain.User;
import com.lilo.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginController {
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	@PostMapping("/signup")
	public String registerUser(@ModelAttribute User user) {
		user.setId(null);
		user.setEmail(user.getEmail().toLowerCase());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole("USER");
		userService.save(user);

		return "redirect:/";
	}

	@GetMapping("/login")
	public String getLoginPage(Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated())
			return "redirect:/";
		
		return "login.html";
	}

	@GetMapping("/signup")
	public String getMethodName(Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated())
			return "redirect:/";
		
		return "signup.html";
	}

}
