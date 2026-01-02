package com.lilo.controller;

import com.lilo.model.User;
import com.lilo.model.dto.UserProfileOutputDTO;
import com.lilo.service.UserService;
import com.lilo.shared.WebConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/api/v1/profiles")
@RestController
public class ProfilesController extends BaseController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable(name = "userId", required = true) long userId) {

        Optional<User> storedUser = userService.findById(userId);
        if (storedUser.isEmpty())
            return buildErrorResponse(HttpStatus.NOT_FOUND, "User not found!");

        String profilePictureUrl;
        if(storedUser.get().getProfilePicture() != null) {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            String path = WebConstants.profilePictureUrlPattern.replace("**", "");
            profilePictureUrl = String.format("%s%s%s",
                    baseUrl,
                    path,
                    storedUser.get().getProfilePicture()
            );
        }else
            profilePictureUrl = null;

        UserProfileOutputDTO generatedUserProfile = UserProfileOutputDTO.builder().id(userId)
                                                                                    .fullName(storedUser.get().getName())
                                                                                    .email(storedUser.get().getEmail())
                                                                                    .profilePicture(profilePictureUrl)
                                                                                    .build();

        return buildSuccessResponse(generatedUserProfile);
    }


}
