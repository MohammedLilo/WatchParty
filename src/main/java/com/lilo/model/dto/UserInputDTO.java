package com.lilo.model.dto;

import com.lilo.model.User;
import com.lilo.shared.annotations.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserInputDTO {

    @PhoneNumber(message = "Invalid phone number, example valid phone number: +9647802999569")
    private String phoneNumber;

    @NotBlank(message = "Email field is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 1, max = 255, message = "FullName must be of 1 to 255 total characters")
    private String fullName;

    public UserInputDTO(User user) {
        this.phoneNumber = user.getPhoneNumber();
        this.email = user.getEmail();
        this.fullName = user.getName();
    }

    public static UserInputDTO fromUser(User authenticatedUser) {
        return new UserInputDTO(authenticatedUser);
    }
}

