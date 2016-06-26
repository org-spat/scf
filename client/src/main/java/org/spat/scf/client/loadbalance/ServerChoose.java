package org.spat.scf.client.loadbalance;
/**
 * 选择存储服务器名
 * 
 * */
public class ServerChoose {
	private int serviceCount;
	private String[] serverName;
	
	public ServerChoose(int serviceCount, String[] serverName) {
		this.serverName = serverName;
		this.serviceCount = serviceCount;
	}
	
	public int getServiceCount() {
		return serviceCount;
	}
	public void setServiceCount(int serviceCount) {
		this.serviceCount = serviceCount;
	}
	public String[] getServerName() {
		return serverName;
	}
	public void setServerName(String[] serverName) {
		this.serverName = serverName;
	}
	
}
