package org.spat.scf.client.configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.spat.scf.client.SCFConst;

public class ManageProfile {
	public static String ManageFilePath = SCFConst.CONFIG_PATH;
	
	public static final ManageProfile manageProfile = new ManageProfile();
	
	private static Properties pro = new Properties();
	
	static {
		try {
			InputStream is = new FileInputStream(new File(ManageFilePath));
			pro.load(is);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static final String MANAGE_URL = "manage_url";
	public static String getManageUrl() {
		return pro.getProperty(MANAGE_URL);
	}
	
	private static final String MANAGE_PORT = "manage_port";
	public static int getManagePort() {
		return Integer.valueOf(pro.getProperty(MANAGE_PORT));
	}
}
