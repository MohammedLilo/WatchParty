package com.lilo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartySyncMessage {
	private long userId;
	private String userName;
	private String event;
	private String videoUrl;
	private Double videoCurrentTime;
	private Long eventDateTime;

}
