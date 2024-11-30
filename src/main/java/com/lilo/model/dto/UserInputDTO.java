package com.lilo.model.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class UserInputDTO {
    private String phoneNumber;
    private String email;
    private String password;
    private String name;

}

