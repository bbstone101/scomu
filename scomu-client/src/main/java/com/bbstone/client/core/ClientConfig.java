package com.bbstone.client.core;

import java.io.File;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientConfig {

	private static Configurations configs = new Configurations();
	
	private static Configuration config() {
		try {
			Configuration config = configs.properties(new File("config.properties"));
			return config;
		} catch (ConfigurationException cex) {
			log.error("parse config.properties error", cex);
			return null;
		}
	}
	
	public static int retryIntvl = config().getInt("scomu.conn.retry.initial.intvl", 3);
	public static int retryMulti = config().getInt("scomu.conn.retry.intvl.multi", 2);
	public static int retryMax = config().getInt("scomu.conn.retry.max", 300);
	
	public static int heartBeatEnabled = config().getInt("scomu.heartbeat.enabled", 0);
	
	
	
	

}
