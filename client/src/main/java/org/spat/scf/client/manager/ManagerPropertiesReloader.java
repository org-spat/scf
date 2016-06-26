package org.spat.scf.client.manager;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


public final class ManagerPropertiesReloader extends BaseFileReloader{

	private static final long INITIAL_DELAY = 1;
	private static final long DELAY = 5;
	
	static final ManagerPropertiesReloader instance = new ManagerPropertiesReloader(ManagerProperties.managerFilePath, INITIAL_DELAY, DELAY);
	private ManagerProperties managerProps;
	
	protected ManagerPropertiesReloader(String filePath, long initialDelay, long delay) {
		super(filePath, initialDelay, delay);
	}
	
	void setOpenapiProperties(ManagerProperties managerProps){
		this.managerProps = managerProps;
	}

	@Override
	protected boolean reload() throws Exception {
		if(managerProps==null)
			return false;
		
		Properties properties = new Properties();
		File managerPropsFile = super.getFile();
		if(!managerPropsFile.exists()) {
			properties.setProperty(ManagerProperties.ADDRS, "127.0.0.1");
			properties.setProperty(ManagerProperties.PORT, "7080");
			managerProps.setBooted();
			managerProps.setProperty(properties);
			return true;
		}
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(managerPropsFile);
			properties.load(fis);
			managerProps.setBooted();
			managerProps.setProperty(properties);
			return true;
		} finally {
			if(fis!=null)
				fis.close();
		}
	}
	

}
