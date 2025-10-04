package com.lilo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyMemberDTO {
    private Long userId;
    private String name;
    private Instant joinTime;
}