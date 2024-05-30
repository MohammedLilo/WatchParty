package com.lilo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lilo.domain.User;
import com.lilo.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	@GetMapping("/user-info")
	public Map<String, String> getUserInfo(Authentication authentication) {
	    User user = userService.findByEmail(authentication.getName());
	    
	    Map<String, String> userInfo = new HashMap<>();
	    userInfo.put("email", user.getEmail());
	    userInfo.put("name", user.getName()); 
	    return userInfo;
	}
}
