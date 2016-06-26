package org.spat.scf.server.performance;

public class MonitorGC {
	
	private long yGcCount;	
	private long yGcTime;	
	private long fGcCount;	
	private long fGcTime;	
	private long gcTime;
	
	public long getyGcCount() {
		return yGcCount;
	}
	
	public void setyGcCount(long yGcCount) {
		this.yGcCount = yGcCount;
	}
	
	public long getyGcTime() {
		return yGcTime;
	}
	
	public void setyGcTime(long yGcTime) {
		this.yGcTime = yGcTime;
	}
	
	public long getfGcCount() {
		return fGcCount;
	}
	
	public void setfGcCount(long fGcCount) {
		this.fGcCount = fGcCount;
	}
	
	public long getfGcTime() {
		return fGcTime;
	}
	
	public void setfGcTime(long fGcTime) {
		this.fGcTime = fGcTime;
	}
	
	public long getGcTime() {
		return gcTime;
	}
	
	public void setGcTime(long gcTime) {
		this.gcTime = gcTime;
	}
	
}
