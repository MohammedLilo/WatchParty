package com.lilo.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PartyInputDTO {

    @Size(max = 50, message = "PartyName can't be longer than 50 characters")
    private String PartyName;
}
