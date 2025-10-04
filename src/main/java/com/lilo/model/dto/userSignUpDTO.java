package com.lilo.model.dto;


import com.lilo.shared.annotations.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class userSignUpDTO {
    @PhoneNumber(message = "Invalid phone number, example valid phone number: +9647802999569")
    private String phoneNumber;

    @NotBlank(message = "Email field is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 60, message = "Password must be of 6 to 60 total characters")
    private String password;

    @Size(min = 1, max = 255, message = "FullName must be of 1 to 255 total characters")
    private String fullName;
}

