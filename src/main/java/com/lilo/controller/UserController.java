package com.lilo.controller;


import com.lilo.model.dto.UserInputDTO;
import com.lilo.operationResult.TableOperationResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.lilo.model.User;
import com.lilo.model.dto.UserOutputDTO;
import com.lilo.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/api/v1/users")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController extends BaseController {
	private final UserService userService;

	@GetMapping("/me")
	public ResponseEntity<?> getUserOwnData(@AuthenticationPrincipal User authenticatedUser) {
		return  buildSuccessResponse(UserOutputDTO.fromUser(authenticatedUser));
	}

    @PatchMapping
    public ResponseEntity<?> updateUserData(@AuthenticationPrincipal User authenticatedUser, @Valid @RequestBody UserInputDTO userInputDTO, BindingResult bindingResult) {
        if(bindingResult.hasErrors())
           return buildBindingErrorResponse(bindingResult);

        TableOperationResult userUpdateResult = userService.update(authenticatedUser, userInputDTO);
        if (userUpdateResult.isSuccess())
            return buildSuccessResponse(UserInputDTO.fromUser(authenticatedUser));

        return buildErrorResponse(userUpdateResult);
    }

    @DeleteMapping
	public ResponseEntity<?> deleteUserAccount(@AuthenticationPrincipal User authenticatedUser, HttpServletRequest request) throws ServletException {
        userService.deleteById(authenticatedUser.getId());
        request.logout();
		return ResponseEntity.noContent().build();
	}
}
