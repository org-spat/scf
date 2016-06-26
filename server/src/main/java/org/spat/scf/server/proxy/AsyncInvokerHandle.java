package org.spat.scf.server.proxy;

import org.spat.scf.protocol.sfp.Protocol;
import org.spat.scf.server.contract.context.ExecFilterType;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.context.SCFResponse;
import org.spat.scf.server.contract.context.SecureContext;
import org.spat.scf.server.contract.filter.IFilter;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.performance.monitor.AbandonCount;
import org.spat.scf.server.utility.ExceptionHelper;
import org.spat.utility.expandasync.AsyncInvoker;
import org.spat.utility.expandasync.IAsyncHandler;

/**
 * async service invoke handle
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class AsyncInvokerHandle extends InvokerBase {
	/**
	 * log
	 */
	private final static ILog logger = LogFactory.getLogger(AsyncInvokerHandle.class);
	/**
	 * 异步执行器
	 */
	private AsyncInvoker asyncInvoker;
	private int taskTimeOut = 1000;
	private int inQueueTime = -1;

	public AsyncInvokerHandle() {
		try {
			int workerCount = Global.getSingleton().getServiceConfig().getInt("scf.async.worker.count");
			
			//此处进行servre端窃取修改
			boolean isSteal = Global.getSingleton().getServiceConfig().getBoolean("scf.async.worker.steal");//读取不到 返回false
			int limitSize = Global.getSingleton().getServiceConfig().getInt("scf.async.worker.limitsize");//读取不到 返回0
			boolean mode = Global.getSingleton().getServiceConfig().getBoolean("scf.async.worker.mode");
			if (workerCount > 0) {
				asyncInvoker = AsyncInvoker.getInstance(workerCount, limitSize, isSteal, mode, "Scf Async worker");
			} else {
				asyncInvoker = AsyncInvoker.getInstance();
			}

			String sTaskTimeOut = Global.getSingleton().getServiceConfig()
					.getString("scf.server.tcp.task.timeout");
			if (sTaskTimeOut != null && !"".equals(sTaskTimeOut)) {
				taskTimeOut = Integer.parseInt(sTaskTimeOut);
			}
			String sInQueueTime = Global.getSingleton().getServiceConfig().getString("scf.server.tcp.task.inqueue");
			if(sInQueueTime != null && !"".equals(sInQueueTime)) {
				inQueueTime = Integer.parseInt(sInQueueTime);
			}
			logger.info("async worker steal:" + isSteal);
			logger.info("async worker limitSize:" + limitSize);
			logger.info("async worker mode:" + mode);
			logger.info("async worker count:" + workerCount);
		} catch (Exception e) {
			logger.error("init AsyncInvokerHandle error", e);
		}
	}

	@Override
	public void invoke(final SCFContext context) throws Exception {
		logger.debug("-------------------begin async invoke-------------------");
		
		asyncInvoker.run(taskTimeOut, inQueueTime, new IAsyncHandler() {
			@Override
			public Object run() throws Throwable {
				logger.debug("begin request filter");
				// request filter

				for (IFilter f : Global.getSingleton().getGlobalRequestFilterList()) {
					if (context.getExecFilter() == ExecFilterType.All || context.getExecFilter() == ExecFilterType.RequestOnly) {
						f.filter(context);
					}
				}

				if (context.isDoInvoke()) {
					doInvoke(context);
				}
				if (context.isAsyn()) {
					return context;
				}
				logger.debug("begin response filter");
				// response filter
				for (IFilter f : Global.getSingleton().getGlobalResponseFilterList()) {
					if (context.getExecFilter() == ExecFilterType.All || context.getExecFilter() == ExecFilterType.ResponseOnly) {
						f.filter(context);
					}
				}
				return context;
			}

			@Override
			public void messageReceived(Object obj) {
				try{
					if (obj != null) {
						SCFContext ctx = (SCFContext) obj;
						if (!ctx.isAsyn()) {
							ctx.getServerHandler().writeResponse(ctx);
						}
					} else {
						logger.error("context is null!");
					}
				}finally{
					SCFContext.removeThreadLocal();
				}
			}

			@Override
			public void exceptionCaught(Throwable e) {
				String message = "";
				try {
					
					//context.setError(ex);
					//context.getServerHandler().writeResponse(context);
					
					if (context.getScfResponse() == null) {
						SCFResponse respone = new SCFResponse();
						context.setScfResponse(respone);
					}

					// 任务超时计数
					if(e.getMessage().indexOf("timeout") > 0) {
						try {
							AbandonCount.messageRecv();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					
					
					byte[] desKeyByte = null;
					String desKeyStr = null;
					boolean bool = false;

					Global global = Global.getSingleton();
					if (global != null) {
						// 判断当前服务启用权限认证
						if (global.getGlobalSecureIsRights()) {
							SecureContext securecontext = global.getGlobalSecureContext(context.getChannel().getNettyChannel());
							bool = securecontext.isRights();
							if (bool) {
								desKeyStr = securecontext.getDesKey();
							}
						}
					}

					if (desKeyStr != null) {
						desKeyByte = desKeyStr.getBytes("utf-8");
					}

					Protocol protocol = context.getScfRequest().getProtocol();
					if (protocol == null) {
						protocol = Protocol.fromBytesOnlySFP(context.getScfRequest().getRequestBuffer(), global.getGlobalSecureIsRights(), desKeyByte);
						context.getScfRequest().setProtocol(protocol);
					}
					
                    if (e.getMessage().contains("async task") ) {
                    	message = ExceptionHelper.createErrorMessage(e, context);
                    }
                    
					protocol.setSdpEntity(ExceptionHelper.createError(e));
					context.getScfResponse().setResponseBuffer(protocol.toBytes(Global.getSingleton().getGlobalSecureIsRights(), desKeyByte));
				} catch (Exception ex) {
					context.getScfResponse().setResponseBuffer(new byte[] { 0 });
					logger.error("AsyncInvokerHandle invoke-exceptionCaught error", ex);
				} finally {
					SCFContext.removeThreadLocal();
					AsynBack.contextMap.remove(context.getSessionID());
				}

				context.getServerHandler().writeResponse(context);
				logger.error("AsyncInvokerHandle invoke error" + message, e);
			}
		});
	}
}