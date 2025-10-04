package com.lilo.model.dto;

import com.lilo.model.User;
import lombok.Data;

@Data
public class UserLoginOutputDTO {
    private long id;
    private String name;
    private String email;
    private String token;

    private UserLoginOutputDTO(User user,  String token) {
    this.id = user.getId();
    this.name = user.getName();
    this.email = user.getEmail();
    this.token = token;
    }
    public static UserLoginOutputDTO fromUser(User user, String token) {
        return new UserLoginOutputDTO(user, token);
    }

}
