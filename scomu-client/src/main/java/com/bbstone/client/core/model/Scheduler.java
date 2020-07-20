package com.bbstone.client.core.model;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Scheduler {
	
	private ScheduledExecutorService scheduledExecutorService;
	private Runnable command;
	private long initialDelay;
	private long period;
	private TimeUnit unit;
	
	public static Scheduler from(
			ScheduledExecutorService scheduledExecutorService,
			Runnable command,
			long initialDelay,
			long period,
			TimeUnit unit) {
		return new Scheduler(scheduledExecutorService, command, initialDelay, period, unit);
	}
	
	

}
