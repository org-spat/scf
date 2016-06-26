package org.spat.scf.client.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.spat.scf.client.SCFConst;
import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;

/**
 * key load 
 * @author haoxb
 */
public abstract class KeyLoad {
	private static ILog logger = LogFactory.getLogger(KeyLoad.class);
	
	public abstract void analysis();
	
	public String run() throws Exception{
		return this.readFileByLines(SCFConst.KEY_CONFIG_PATH);
	}
	
	public String run(String fileName) throws Exception{
		return this.readFileByLines(fileName);
	}
	
    private String readFileByLines(String fileName) throws Exception {
        File file = new File(fileName);
        if(!file.exists()){
        	logger.warn("scfkey.key file not found! default file path is "+SCFConst.DEFAULT_ROOT_PATH);
        	return null;
        }
        BufferedReader reader = null;
        StringBuffer sbuffer = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                sbuffer.append(tempString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                	e1.printStackTrace();
                }
            }
        }
        return sbuffer.toString();
    }
    
}
