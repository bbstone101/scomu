package com.bbstone.client.core.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.bbstone.client.core.ClientContextHolder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpeedChangeExecutor {
	
	private boolean switcher = false;
	private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	private List<SpeedChangeListner> scls = new ArrayList<>();
	
	private long inputSpeed = 0L;
	private String inputSpeedUnit = "B";
	
	private long outputSpeed = 0L;
	private String outputSpeedUnit = "B";
	// ------------------------
	private long initialDelay = 6L; //60L;
	private long period = 3L; //every 3 sec update/calculate transfer speed

	public void start(String connId) {
		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (switcher) {
					calc(connId);
				}
			}
		}, initialDelay, period, TimeUnit.SECONDS);
	}

	public void calc(String connId) {
		// reset startTime
		this.inputSpeed = ClientContextHolder.getContext(connId).getTotalInBytes()/period;
		this.outputSpeed = ClientContextHolder.getContext(connId).getTotalOutBytes()/period;
		// 
		normalizeSpeedDisplay();
		// 
		log.debug("current input speed: {} {}, connId: {}", inputSpeed, inputSpeedUnit, connId);
		log.debug("current output speed: {} {}, connId: {}", outputSpeed, outputSpeedUnit, connId);
		// reset total bytes
		ClientContextHolder.getContext(connId).resetTotalInBytes();
		ClientContextHolder.getContext(connId).resetTotalOutBytes();
		// notify I/O speed change...
		log.info("speed change listner size: {}", scls.size());
		for (SpeedChangeListner scl : scls) {
			scl.onInputSpeedChange(connId, this.inputSpeed, this.inputSpeedUnit);
			scl.onOuputSpeedChange(connId, this.outputSpeed, this.outputSpeedUnit);
		}
	}
	
	public void enable() {
		switcher = true;
	}
	
	public void disable() {
		switcher = false;
	}
	
	private void normalizeSpeedDisplay() {
		if (inputSpeed > 2 * 1024) {
			this.inputSpeed = inputSpeed/1024;
			inputSpeedUnit = "KB";
		} else if (inputSpeed > 3 * 1024 * 1024) {
			this.inputSpeed = inputSpeed/(1024 * 1024);
			inputSpeedUnit = "MB";
		}
		// output speed & unit
		if (this.outputSpeed > 2 * 1024) {
			this.outputSpeed = outputSpeed/1024;
			outputSpeedUnit = "KB";
		} else if (outputSpeed > 3 * 1024 * 1024) {
			this.outputSpeed = outputSpeed/(1024 * 1024);
			outputSpeedUnit = "MB";
		}
	}

	
	public void addSpeedChangeListner(SpeedChangeListner scl) {
		this.scls.add(scl);
	}
	
}
