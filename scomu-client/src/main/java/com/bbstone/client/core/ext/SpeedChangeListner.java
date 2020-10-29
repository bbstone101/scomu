package com.bbstone.client.core.ext;

/**
 * 
 * Client I/O Speed change listner
 * 
 * @author bbstone101
 *
 */
public interface SpeedChangeListner {

	/**
	 * read data from channel speed change
	 * @param connId
	 */
	public void onInputSpeedChange(String connId, long speed, String speedUnit);
	
	/**
	 * write data to channel speed change
	 * @param connId
	 */
	public void onOuputSpeedChange(String connId, long speed, String speedUnit);
	
}
