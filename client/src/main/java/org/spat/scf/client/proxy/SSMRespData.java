package org.spat.scf.client.proxy;

import com.google.gson.Gson;

public class SSMRespData {
	private int version = 0;
	private SSMRespDataType flag = SSMRespDataType.NORMAL;
	private String serverName;
	private boolean isConfigChanged;
	private long lastChangeTime;
	private String config;
	
	public byte[] dataCreate() throws Exception {
		Gson gson = new Gson();
		String gs = gson.toJson(this);
		return gs.getBytes("UTF-8");
	}

	public static SSMRespData fromBytes(byte[] buf) throws Exception {
		Gson gson = new Gson();
		String str = new String(buf,"UTF-8");
		SSMRespData rp = gson.fromJson(str, SSMRespData.class);
		return rp;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	public SSMRespDataType getFlag() {
		return flag;
	}

	public void setFlag(SSMRespDataType flag) {
		this.flag = flag;
	}

	public String getServerName() {
		return serverName;
	}
	
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	public boolean getIsConfigChanged() {
		return isConfigChanged;
	}
	
	public void setIsConfigChanged(boolean isConfigChanged) {
		this.isConfigChanged = isConfigChanged;
	}
	
	public long getLastChangeTime() {
		return lastChangeTime;
	}
	
	public void setLastChangeTime(long lastChangeTime) {
		this.lastChangeTime = lastChangeTime;
	}
	
	public String getConfig() {
		return config;
	}
	
	public void setConfig(String config) {
		this.config = config;
	}
	
	enum SSMRespDataType{
		CANCEL,NORMAL;
	}
}
