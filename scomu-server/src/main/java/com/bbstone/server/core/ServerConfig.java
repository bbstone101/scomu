package com.bbstone.server.core;

import java.io.File;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import com.bbstone.comm.util.CipherUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerConfig {

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
	
	public static int port = config().getInt("server.port", 8899);
	public static String host = config().getString("server.host", "0.0.0.0");
	
	public static String serverId = CipherUtil.md5(host+port);
	
	// seconde
	public static int idleTimeout = config().getInt("scomu.core.client.idle.timeout", 120);
	

	
	
	public static String authDB = config().getString("scomu.auth.db", "demo");
	public static String authHost = config().getString("scomu.auth.server.host", "127.0.0.1");
	public static int authPort = config().getInt("scomu.auth.server.port", 6379);
	public static String authUsername = config().getString("scomu.auth.server.username", "");
	public static String authPassword = config().getString("scomu.auth.server.password", "Welcome1");
	public static String demoUsername = config().getString("scomu.auth.demo.username", "demo");
	public static String demoPassword = config().getString("scomu.auth.demo.password", "demopass");
	
	
	
	/**
	 * max_sessions_per_conn, default is 5
	 */
	public static int max_sessions_per_conn = config().getInt("scomu.core.max_sessions_per_conn", 5);
	
	public static String free_access_cmds = config().getString("scomu.auth.filter.free.access.cmds", "AUTH_START,AUTH_ANSWER");
	
	
	
	
	
	

}
