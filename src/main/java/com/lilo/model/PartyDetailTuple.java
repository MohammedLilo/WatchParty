package com.lilo.model;

import com.lilo.model.dto.PartySyncEventInputDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyDetailTuple {
	private int membersCount = 1;
	private PartySyncEventInputDTO latestPartySyncEventInputDTO;
	private PartySyncEventInputDTO previousPartySyncEventInputDTO;

	public void incrementMembersCount() {
		this.membersCount++;
	}

	public void decrementMembersCount() {
		this.membersCount--;
	}

	public PartyDetailTuple(PartySyncEventInputDTO latestPartySyncEventInputDTO) {
		this.latestPartySyncEventInputDTO = latestPartySyncEventInputDTO;
	}
}
