package org.spat.scf.client.logger;

/**
 * LogFactory
 *
 * @author Service Platform Architecture Team 
 */
public final class LogFactory {

    /**
     * Get an instance of a logger object.
     *
     * @param cls the Class to log from
     * @return Logger the logger instance
     */
    public static ILog getLogger(Class<?> cls) {
        return new FileLog(cls);
    }
}
