package com.lilo.model.dto;

import com.lilo.enums.PartyMemberEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyMemberEventOutputDTO {

    private long userId;
    private PartyMemberEvent event;
    private Instant timestamp;
}
