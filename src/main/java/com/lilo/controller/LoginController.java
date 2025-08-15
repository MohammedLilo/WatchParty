package com.lilo.controller;

import com.lilo.model.dto.UserDTO;
import com.lilo.model.dto.UserInputDTO;
import com.lilo.operationResult.TableOperationResult;
import com.lilo.service.UserAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lilo.model.User;
import com.lilo.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginController {
	private final UserAuthService userAuthService;

	@PostMapping("/signup")
	public ResponseEntity<String> registerUser( UserInputDTO userInputDTO) {
			User newUser = User.FromUserInputDTO(userInputDTO);
			TableOperationResult userSavingResult = userAuthService.save(newUser);
		if(userSavingResult.isSuccess())
			return ResponseEntity.status(HttpStatus.CREATED).body("signed up successfully");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(userSavingResult.getErrorMessage());
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
