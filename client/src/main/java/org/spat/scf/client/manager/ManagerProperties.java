package org.spat.scf.client.manager;

import java.util.Properties;


public class ManagerProperties {
	
	static String managerFilePath = System.getProperty("user.dir") + "/scfclient.properties";
	
	public static final ManagerProperties instance = new ManagerProperties();
	
	private ManagerProperties(){
		ManagerPropertiesReloader.instance.setOpenapiProperties(this);
	}
	
	private boolean boot;
	private Properties props;
	
	synchronized void setBooted(){
		this.boot = true;
	}
	
	boolean booted(){
		return this.boot;
	}
	
	private String getProperty(String key) {
		if(!booted())
			try {
				ManagerPropertiesReloader.instance.reload();
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return props.getProperty(key);
	}

	void setProperty(Properties props) {
		this.props = props;
	}
	
	/**
	 * 获取IP
	 */
	private static final String ADDR = "addr";
	public String getManagerIp() {
		return getProperty(ADDR);
	}
	
	public static final String ADDRS = "addrs";
	public String[] getManagerIps() {
		return getProperty(ADDRS).split(":");
	}
	
	public static final String PORT = "port";
	public int getManagerPort() {
		return Integer.valueOf(getProperty(PORT));
	}
	
	private static final String SERVICE = "service";
	public String[] getServices() {
		String[] services = getProperty(SERVICE).trim().split(",");
		return services;
	}	

	public static void main(String[] args) {
		System.out.println(instance.getServices()[0]);
	}
	
}
