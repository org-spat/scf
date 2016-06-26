package org.spat.scf.server.contract.log;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.SCFContext;

public final class AsynLog4jLogger implements ILog{
	
	private static boolean noExtraInfo = false;

	static {
		try {
			noExtraInfo = Global.getSingleton().getServiceConfig().getBoolean("scf.log.info.noextra");
		} catch (Exception e) {
		
		} finally {
			System.out.println("noExtraInfo:" + noExtraInfo);
		}
	}
	
	private Logger logger = null;
	
	public AsynLog4jLogger(Class<?> cls) {
		logger = LogManager.getLogger(cls);
	}
	
	public AsynLog4jLogger(String name) {
		logger = LogManager.getLogger(name);
	}
	
	private static final String FQCN = AsynLog4jLogger.class.getName();
	
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
		} else {
			return msg;
		}
	}
	
	@Override
	public void fine(String message) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.DEBUG, getLogMsg(message));
	}

	@Override
	public void config(String message) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.DEBUG, getLogMsg(message));
	}

	@Override
	public void info(String message) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.INFO, getLogMsg(message));
	}

	@Override
	public void warning(String message) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.WARN, getLogMsg(message));
	}
	
	@Override
	public void trace(String message) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.TRACE, getLogMsg(message));
	}

	@Override
	public void trace(String message, Throwable t) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.TRACE, getLogMsg(message), t);
	}

	@Override
	public void trace(Throwable t) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.TRACE, getLogMsg(""), t);
	}
	
	@Override
	public void debug(String message) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.DEBUG, getLogMsg(message));
	}

	@Override
	public void debug(String message, Throwable t) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.DEBUG, getLogMsg(message), t);
	}

	@Override
	public void info(String message, Throwable t) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.INFO, getLogMsg(message), t);
	}

	@Override
	public void warn(String message) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.WARN, getLogMsg(message));
	}

	@Override
	public void warn(String message, Throwable t) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.WARN, getLogMsg(message), t);
	}

	@Override
	public void error(String message) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.ERROR, getLogMsg(message));
	}

	@Override
	public void error(String message, Throwable t) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.ERROR, getLogMsg(message), t);
	}

	@Override
	public void error(Throwable e) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.DEBUG, getLogMsg(""), e);
	}

	@Override
	public void fatal(String message) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.FATAL, getLogMsg(message));
	}

	@Override
	public void fatal(String message, Throwable t) {
		LogAsyncWorker.getLogAsyncWorker().execute(logger, FQCN, Level.FATAL, getLogMsg(message), t);
	}

}
