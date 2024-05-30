package com.lilo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartySynchMessage {
	private String event;
	private String videoUrl;
	private double currentTime;
}
