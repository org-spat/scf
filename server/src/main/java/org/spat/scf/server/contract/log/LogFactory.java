package org.spat.scf.server.contract.log;

/**
 * A class to get an instance for a logger object
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public final class LogFactory {
	
	/**
	 * Get an instance of a logger object.
	 * 
	 * @param cls the Class to log from
	 * @return Logger the logger instance
	 */
	public static ILog getLogger(Class<?> cls) {
		String logType = System.getProperty("asyn.log.switch");
		if ("true".equalsIgnoreCase(logType)) {
			return new AsynLog4jLogger(cls);
		} else {
			return new Log4jLogger(cls);
		}
	}
	
	public static ILog getLogger(String name) {
		String logType = System.getProperty("asyn.log.switch");
		if ("true".equalsIgnoreCase(logType)) {
			return new AsynLog4jLogger(name);
		} else {
			return new Log4jLogger(name);
		}
	}
	
	public static ILog getASynLogger(Class<?> cls) {
		return new AsynLog4jLogger(cls);
	}
	
	public static ILog getASynLogger(String name) {
		return new AsynLog4jLogger(name);
	}
}