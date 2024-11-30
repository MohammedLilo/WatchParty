package com.lilo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyDetailTuple {
	private int membersCount = 1;
	private PartySyncMessage latestPartySyncMessage;
	private PartySyncMessage previousPartySyncMessage;

	public void incrementMembersCount() {
		this.membersCount++;
	}

	public void decrementMembersCount() {
		this.membersCount--;
	}

	public PartyDetailTuple(PartySyncMessage latestPartySyncMessage) {
		this.latestPartySyncMessage = latestPartySyncMessage;
	}
}
