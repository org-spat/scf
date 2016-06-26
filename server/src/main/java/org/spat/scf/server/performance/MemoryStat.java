package org.spat.scf.server.performance;


public class MemoryStat{
	
	private long init;	
	private long used;	
	private long committed;	
	private long max;	
	private double Percentage;
	
	public double getPercentage() {
		return Percentage;
	}

	public void setPercentage(double percentage) {
		Percentage = percentage;
	}

	public long getInit() {
		return init;
	}

	public void setInit(long init) {
		this.init = init;
	}

	public long getUsed() {
		return used;
	}

	public void setUsed(long used) {
		this.used = used;
	}

	public long getCommitted() {
		return committed;
	}

	public void setCommitted(long committed) {
		this.committed = committed;
	}

	public long getMax() {
		return max;
	}

	public void setMax(long max) {
		this.max = max;
	}

	
}
