package com.lilo.controller;

import com.lilo.model.User;
import com.lilo.model.dto.*;
import com.lilo.operationResult.TableOperationResult;
import com.lilo.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RequestMapping("/api/v1/auth")
@RestController
@RequiredArgsConstructor
public class AuthController extends BaseController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody userSignUpDTO userSignUpDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return buildBindingErrorResponse(bindingResult);

        User newUser = User.fromUserInputDTO(userSignUpDTO);
        TableOperationResult userSavingResult = authService.save(newUser);
        if (userSavingResult.isSuccess()) {
            URI location = MvcUriComponentsBuilder
                    .fromMethodCall(on(ProfilesController.class).getUserProfile(newUser.getId()))
                    .build()
                    .toUri();
            return ResponseEntity.created(location)
                                 .body(ApiResponse.withSuccess("signed up successfully"));
        }
        return ResponseEntity.status(userSavingResult.getSuggestedStatusCode())
                             .body(ApiResponse.withError(new ApiError(userSavingResult.getSuggestedStatusCode(),
                                                                      userSavingResult.getErrorMessage())));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginInputDTO userLoginDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return buildBindingErrorResponse(bindingResult);

        Optional<User> storedUser = authService.validateUserCredentials(userLoginDto.getEmail(), userLoginDto.getPassword());
        if (storedUser.isEmpty()) {
            ApiResponse<?> response = ApiResponse.withError(new ApiError(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        UserLoginOutputDTO userLoginOutputDTO = UserLoginOutputDTO.fromUser(storedUser.get(), authService.generateToken(storedUser.get()));
        return ResponseEntity.ok(ApiResponse.withSuccess(userLoginOutputDTO));
    }
}
