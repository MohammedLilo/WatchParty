package com.lilo.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyDetailsDTO {
    private String id;
    private String partyName;
    private Long ownerUserId;
    private String ownerName;
    private String currentVideoUrl;
    private String latestSyncEventPayload;
    private boolean isPrivate;
    private String thumbnailUrl;
    private List<PartyMemberDTO> members;
}
