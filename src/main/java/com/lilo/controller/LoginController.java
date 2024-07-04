package com.lilo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lilo.domain.User;
import com.lilo.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginController {
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	@PostMapping("/signup")
	public String registerUser(User user) {
		user.setId(null);
		user.setEmail(user.getEmail().toLowerCase());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole("USER");
		try {
			userService.save(user);
		} catch (RuntimeException e) {
			return "redirect:/signup?err=" + e.getMessage();
		}
		return "redirect:/";
	}

	@GetMapping("/login")
	public String getLoginPage(Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated())
			return "redirect:/";

		return "login.html";
	}

	@GetMapping("/signup")
	public String getSignupPage(@RequestParam(value = "err", required = false) String err,
			Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated())
			return "redirect:/";

		return "signup.html?err=" + err;
	}

}
