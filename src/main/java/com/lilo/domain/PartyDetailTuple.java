package com.lilo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyDetailTuple {
	private int membersCount;
	private PartySyncMessage latestPartySyncMessage;
	private PartySyncMessage previousPartySyncMessage;

	public void incrementMembersCount() {
		this.membersCount++;
	}

	public void decrementMembersCount() {
		this.membersCount--;
	}

	public PartyDetailTuple(int membersCount, PartySyncMessage latestPartySyncMessage) {
		this.membersCount = membersCount;
		this.latestPartySyncMessage = latestPartySyncMessage;
	}
}
