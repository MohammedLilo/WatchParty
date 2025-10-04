package com.lilo.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileOutputDTO {
    private long id;
    private String fullName;
    private String email;
//    private String phoneNumber;
    private String profilePicture;

}
