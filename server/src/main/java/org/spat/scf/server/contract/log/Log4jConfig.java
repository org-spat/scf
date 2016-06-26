package org.spat.scf.server.contract.log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * This class will do the configuration of Log4j
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class Log4jConfig {
	
	public static void configure(String configFilePath) {
		 try
		    {
		      ConfigurationSource source = new ConfigurationSource(new FileInputStream(configFilePath));
		      Configurator.initialize(null, source);
		    } catch (FileNotFoundException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
	}

}