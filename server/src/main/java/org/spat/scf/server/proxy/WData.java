package org.spat.scf.server.proxy;

public class WData {
	private int sessionID;
	
	private long time;
	
	public WData(int s, long t){
		this.sessionID = s;
		this.time = t;
	}
	
	public int getSessionID() {
		return sessionID;
	}

	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}
