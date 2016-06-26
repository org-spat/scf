package org.spat.scf.server.performance;

public class MonitorMemory{

	private MemoryStat old;
	private MemoryStat eden;
	private MemoryStat survivor;
	private MemoryStat codeCache;
	private MemoryStat perm;
	
	public MemoryStat getOld() {
		return old;
	}

	public void setOld(MemoryStat old) {
		this.old = old;
	}

	public MemoryStat getEden() {
		return eden;
	}

	public void setEden(MemoryStat eden) {
		this.eden = eden;
	}

	public MemoryStat getSurvivor() {
		return survivor;
	}

	public void setSurvivor(MemoryStat survivor) {
		this.survivor = survivor;
	}

	public MemoryStat getCodeCache() {
		return codeCache;
	}

	public void setCodeCache(MemoryStat codeCache) {
		this.codeCache = codeCache;
	}

	public MemoryStat getPerm() {
		return perm;
	}

	public void setPerm(MemoryStat perm) {
		this.perm = perm;
	}

	
}
