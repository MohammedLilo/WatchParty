package com.lilo.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lilo.model.Party;
import com.lilo.shared.WebConstants;
import lombok.Data;

import java.time.Instant;
@Data
public class PartySummaryDTO {
    private String id;
    private long ownerUserId;
    private String name;
    @JsonProperty("isPrivate")
    private boolean isPrivate;
    private Instant createdAt;
    private String thumbnailUrl;
    private PartySummaryDTO(Party party, String thumbnailUrl) {
    this.id = party.getId();
    this.ownerUserId = party.getOwnerUserId();
    this.name = party.getName();
    this.isPrivate = party.isPrivate();
    this.createdAt = party.getCreatedAt();
    this.thumbnailUrl = thumbnailUrl;
    }
public static PartySummaryDTO fromParty(Party party, String baseUrl) {
    String path = WebConstants.thumbnailsUrlPattern.replace("**", "");
    String thumbnailUrl = String.format("%s%s%s",
            baseUrl,
            WebConstants.thumbnailsUrlPattern.replace("**", ""),
            party.getThumbnailFileName()
    );

    return new PartySummaryDTO(party, thumbnailUrl);
    }
}
