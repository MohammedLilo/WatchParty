package com.lilo.model.dto;

import com.lilo.enums.PartyEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartySyncEventInputDTO {
    private PartyEvent event;
    private String videoUrl;
    private Double videoCurrentTime;
    private Long eventDateTime;

}
