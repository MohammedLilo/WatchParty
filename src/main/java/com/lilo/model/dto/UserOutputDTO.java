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

	private UserOutputDTO(User user) {
		this.id = user.getId();
		this.email = user.getEmail();
		this.name = user.getName();
		this.phoneNumber = user.getPhoneNumber();
		this.partyId = user.getPartyId();
	}
public static UserOutputDTO fromUser(User user) {
        return new UserOutputDTO(user);
}
}
