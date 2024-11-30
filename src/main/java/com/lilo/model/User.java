package com.lilo.model;

import com.lilo.model.dto.UserInputDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "user")
@Entity
@Data
@NoArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable=false,unique=true)
	private String email;

	private String password;
	private String name;
	@Column(name="phone_number", nullable=false,unique=true,length=14)
	private String phoneNumber;
	@Column(name = "party_id")
	private String partyId;

	private User(UserInputDTO userInputDTO){
		email = userInputDTO.getEmail();
		password = userInputDTO.getPassword();
		name = userInputDTO.getName();
		phoneNumber = userInputDTO.getPhoneNumber();
	}

	public static  User FromUserInputDTO(UserInputDTO userInputDTO){
		return new User(userInputDTO);
	}
}
