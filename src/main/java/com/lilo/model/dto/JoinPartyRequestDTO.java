package com.lilo.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JoinPartyRequestDTO {
    @NotEmpty(message = "Party Id cannot be empty")
    private String partyId;
}
