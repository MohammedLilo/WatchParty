package com.lilo.controller;


import com.lilo.model.dto.ApiResponse;
import com.lilo.model.dto.UserInputDTO;
import com.lilo.operationResult.TableOperationResult;
import com.lilo.shared.WebConstants;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

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
    @PatchMapping("/profile-picture")
    public ResponseEntity<?> setProfilePicture(@RequestPart MultipartFile profilePictureMultipartFile, @AuthenticationPrincipal User authenticatedUser) throws IOException {
        userService.updateProfilePicture(authenticatedUser,  profilePictureMultipartFile);
        String path = WebConstants.profilePictureUrlPattern.replace("**", "");
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(path)
                .path(authenticatedUser.getProfilePicture())
                .build()
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.withSuccess("Profile Picture updated successfully!"));
    }

    @DeleteMapping
	public ResponseEntity<?> deleteUserAccount(@AuthenticationPrincipal User authenticatedUser, HttpServletRequest request) throws ServletException {
        userService.deleteById(authenticatedUser.getId());
        request.logout();
		return ResponseEntity.noContent().build();
	}
}
