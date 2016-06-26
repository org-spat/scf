package org.spat.scf.server.contract.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.SCFContext;

public final class Log4jLogger implements ILog {
	private static boolean noExtraInfo = false;

	private transient Logger logger = null;
	private ExtendedLoggerWrapper log = null;
	
	static {
		try {
			noExtraInfo = Global.getSingleton().getServiceConfig().getBoolean("scf.log.info.noextra");
		} catch (Exception e) {
		} finally {
			System.out.println(new StringBuilder().append("noExtraInfo:").append(noExtraInfo).toString());
		}
	}

	private static final String FQCN = Log4jLogger.class.getName();

	public Log4jLogger(Class<?> cls) {
		this.logger = LogManager.getLogger(cls);
		this.log = new ExtendedLoggerWrapper((ExtendedLogger) this.logger, this.logger.getName(),
				this.logger.getMessageFactory());
	}

	public Log4jLogger(String name) {
		this.logger = LogManager.getLogger(name);
		this.log = new ExtendedLoggerWrapper((ExtendedLogger) this.logger, this.logger.getName(),
				this.logger.getMessageFactory());
	}

	private String getLogMsg(String msg) {
		if (!noExtraInfo) {
			StringBuilder sbLog = new StringBuilder();
			sbLog.append(msg);
			SCFContext context = SCFContext.getFromThreadLocal();
			if (context != null) {
				sbLog.append("--");
				sbLog.append("remoteIP:");
				sbLog.append(context.getChannel().getRemoteIP());
				sbLog.append("--remotePort:");
				sbLog.append(context.getChannel().getRemotePort());
			}
			return sbLog.toString();
		}
		return msg;
	}

	public void fine(String message) {
		this.log.logIfEnabled(FQCN, Level.DEBUG, null, getLogMsg(message));
	}

	public void config(String message) {
		this.log.logIfEnabled(FQCN, Level.DEBUG, null, getLogMsg(message));
	}

	public void info(String message) {
		this.log.logIfEnabled(FQCN, Level.INFO, null, getLogMsg(message));
	}

	public void warning(String message) {
		this.log.logIfEnabled(FQCN, Level.WARN, null, getLogMsg(message));
	}

	public void trace(String message) {
		this.log.logIfEnabled(FQCN, Level.TRACE, null, getLogMsg(message));
	}

	public void trace(String message, Throwable t) {
		this.log.logIfEnabled(FQCN, Level.TRACE, null, getLogMsg(message), t);
	}

	public void trace(Throwable t) {
		this.log.logIfEnabled(FQCN, Level.TRACE, null, getLogMsg(""), t);
	}

	public void debug(String message) {
		this.log.logIfEnabled(FQCN, Level.DEBUG, null, getLogMsg(message));
	}

	public void fatal(String message, Throwable t) {
		this.log.logIfEnabled(FQCN, Level.FATAL, null, getLogMsg(message), t);
	}

	public void debug(String message, Throwable t) {
		this.log.logIfEnabled(FQCN, Level.DEBUG, null, getLogMsg(message), t);
	}

	public void info(String message, Throwable t) {
		this.log.logIfEnabled(FQCN, Level.INFO, null, getLogMsg(message), t);
	}

	public void warn(String message) {
		this.log.logIfEnabled(FQCN, Level.WARN, null, getLogMsg(message));
	}

	public void warn(String message, Throwable t) {
		this.log.logIfEnabled(FQCN, Level.WARN, null, getLogMsg(message), t);
	}

	public void error(String message) {
		this.log.logIfEnabled(FQCN, Level.ERROR, null, getLogMsg(message));
	}

	public void error(String message, Throwable t) {
		this.log.logIfEnabled(FQCN, Level.ERROR, null, getLogMsg(message), t);
	}

	public void error(Throwable t) {
		this.log.logIfEnabled(FQCN, Level.ERROR, null, getLogMsg(""), t);
	}

	public void fatal(String message) {
		this.log.logIfEnabled(FQCN, Level.FATAL, null, getLogMsg(message));
	}

	
}