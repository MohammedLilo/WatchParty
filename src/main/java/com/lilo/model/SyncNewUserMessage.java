package com.lilo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncNewUserMessage {
	private String event;
	private String previousEvent;
	private String videoUrl;
	private Double videoCurrentTime;
	private Long eventDateTime;

}
