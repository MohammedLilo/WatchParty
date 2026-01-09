package com.lilo.model.dto;

import com.lilo.model.User;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserOutputDTO {
	private long id;
	private String email;
	private String name;
	private String phoneNumber;
	private String partyId;
    private String profilePictureUrl;

	private UserOutputDTO(User user, String profilePictureUrl) {
		this.id = user.getId();
		this.email = user.getEmail();
		this.name = user.getName();
		this.phoneNumber = user.getPhoneNumber();
		this.partyId = user.getPartyId();
        this.profilePictureUrl = profilePictureUrl;

	}
public static UserOutputDTO fromUser(User user, String profilePictureUrl) {
        return new UserOutputDTO(user, profilePictureUrl);
}
}
