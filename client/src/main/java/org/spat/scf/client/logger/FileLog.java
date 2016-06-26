
package org.spat.scf.client.logger;

import org.spat.scf.client.SCFConst;

/**
 * FileLog
 *
 * @author Service Platform Architecture Team 
 */
public class FileLog implements ILog {

    public org.apache.commons.logging.Log logger = null;

    public FileLog(Class<?> cls) {
        logger = org.apache.commons.logging.LogFactory.getLog(cls);
    }

    @Override
    public void fine(String message) {
        logger.info(SCFConst.VERSION_FLAG + message);
    }

    @Override
    public void config(String message) {
        logger.info(SCFConst.VERSION_FLAG + message);
    }

    @Override
    public void info(String message) {
        logger.info(SCFConst.VERSION_FLAG + message);
    }

    @Override
    public void warning(String message) {
        logger.warn(SCFConst.VERSION_FLAG + message);
    }

    @Override
    public void debug(String message) {
        logger.debug(SCFConst.VERSION_FLAG + message);
    }

    @Override
    public void debug(String message, Throwable t) {
        logger.debug(SCFConst.VERSION_FLAG + message, t);
    }

    @Override
    public void warn(String message) {
        logger.warn(SCFConst.VERSION_FLAG + message);
    }

    @Override
    public void error(String message) {
        logger.error(SCFConst.VERSION_FLAG + message);
    }

    @Override
    public void error(String message, Throwable t) {
        logger.error(SCFConst.VERSION_FLAG + message, t);
    }

    @Override
    public void error(Throwable e) {
        logger.error(e);
    }

    @Override
    public void fatal(String message) {
        logger.fatal(SCFConst.VERSION_FLAG + message);
    }

    @Override
    public void fatal(String message, Throwable t) {
        logger.fatal(SCFConst.VERSION_FLAG + message, t);
    }

    @Override
    public void info(String message, Throwable t) {
        logger.info(SCFConst.VERSION_FLAG + message, t);
    }

    @Override
    public void warn(String message, Throwable t) {
        logger.warn(SCFConst.VERSION_FLAG + message, t);
    }
}
