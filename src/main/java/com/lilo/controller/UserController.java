package com.lilo.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lilo.model.User;
import com.lilo.model.dto.UserDTO;
import com.lilo.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.server.HttpServerResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
	private final UserService userService;

	@GetMapping("/users/user-name")
	public ResponseEntity<Object> getUserInfo(Principal principal) {
		User user = userService.findByEmail(principal.getName());
		if (user == null)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

		Map<String, String> userInfo = new HashMap<>();
		userInfo.put("email", user.getEmail());
		userInfo.put("name", user.getName());
		return ResponseEntity.status(HttpStatus.OK).body(userInfo);
	}

	@GetMapping("/users")
	public ResponseEntity<UserDTO> getMethodName(Principal principal) {
		User user = userService.findByEmail(principal.getName());
		if (user == null)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		return  ResponseEntity.ok(new UserDTO(user));
	}

	@DeleteMapping("/users")
	public ResponseEntity<Void> deleteUserAccount(Principal principal, HttpServletRequest request,
			HttpServerResponse response) {
		User user = userService.findByEmail(principal.getName());

		if (user == null)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		userService.deleteById(user.getId());
		try {
			request.logout();
		} catch (ServletException e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
