package org.spat.scf.server.proxy;

import org.spat.scf.protocol.sdp.RequestProtocol;
import org.spat.scf.protocol.sdp.ResponseProtocol;
import org.spat.scf.protocol.sfp.Protocol;
import org.spat.scf.protocol.utility.KeyValuePair;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.IProxyStub;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.context.SCFResponse;
import org.spat.scf.server.contract.context.StopWatch;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.performance.monitor.FrameExCount;
import org.spat.scf.server.utility.ErrorState;
import org.spat.scf.server.utility.ExceptionHelper;
import org.spat.scf.server.utility.ServiceFrameException;
import org.spat.scf.server.utility.SystemUtils;

public abstract class InvokerBase implements IInvokerHandle {

	/**
	 * log
	 */
	private final static ILog logger = LogFactory.getLogger(InvokerBase.class);
	
	/**
	 * 调用真实服务
	 * @param context
	 */
	void doInvoke(SCFContext context) {
		logger.debug("------------------------------ begin request-----------------------------");

		StringBuffer sbInvokerMsg = new StringBuffer();
		StringBuffer sbIsAsynMsg = new StringBuffer();
		StopWatch sw = context.getStopWatch();
		Object response = null;
		Protocol protocol = null;
		
		try {
		    protocol = context.getScfRequest().getProtocol();
			RequestProtocol request = (RequestProtocol)protocol.getSdpEntity();
			
			sbInvokerMsg.append("protocol version:");
			sbInvokerMsg.append(protocol.getVersion());
			sbInvokerMsg.append("\nfromIP:");
			sbInvokerMsg.append(context.getChannel().getRemoteIP());
			sbInvokerMsg.append("\nlookUP:");
			sbInvokerMsg.append(request.getLookup());
			sbIsAsynMsg.append(request.getLookup());
			sbInvokerMsg.append("\nmethodName:");
			sbInvokerMsg.append(request.getMethodName());
			sbIsAsynMsg.append(request.getMethodName());
			sbInvokerMsg.append("\nparams:");
			
			if(request.getParaKVList() != null){
				for (KeyValuePair kv : request.getParaKVList()) {
					if(kv != null) {
						sbInvokerMsg.append("\n--key:");
						sbInvokerMsg.append(kv.getKey());
						sbIsAsynMsg.append(kv.getKey());
						sbInvokerMsg.append("\n--value:");
						sbInvokerMsg.append(kv.getValue());
					} else {
						logger.error("KeyValuePair is null  Lookup:" + request.getLookup() + "--MethodName:" + request.getMethodName());
					}
				}
			}
			
			logger.debug(sbInvokerMsg.toString());
			logger.debug(sbIsAsynMsg.toString());
			logger.debug("begin get proxy factory");
					
			// get local proxy
			IProxyStub localProxy = Global.getSingleton().getProxyFactory().getProxy(request.getLookup());
			logger.debug("proxyFactory.getProxy finish");
			if (localProxy == null) {
				ServiceFrameException sfe = new ServiceFrameException(
						"method:ProxyHandle.invoke--msg:" + request.getLookup() + "." + request.getMethodName() + " not fond",
						context.getChannel().getRemoteIP(), 
						context.getChannel().getLocalIP(), 
						request,
						ErrorState.NotFoundServiceException, 
						null);
				response = ExceptionHelper.createError(sfe);
				logger.error("localProxy is null", sfe);
			} else {
				logger.debug("begin localProxy.invoke");
				String swInvoderKey = "InvokeRealService_" + request.getLookup() + "." + request.getMethodName();
				sw.startNew(swInvoderKey, sbInvokerMsg.toString());
				sw.setFromIP(context.getChannel().getRemoteIP());
				sw.setLocalIP(context.getChannel().getLocalIP());
				SCFContext.setThreadLocal(context);
				if(AsynBack.asynMap.containsKey(sbIsAsynMsg.toString())) {
					int sessionid = SystemUtils.createSessionId();	
					context.setAsyn(true);
					context.setSessionID(sessionid);
					AsynBack.contextMap.put(sessionid, context);
					AsynBack.swInvokeKeyMap.put(sessionid, swInvoderKey);
					AsynBack.callBackUtil.offer(new WData(sessionid, System.currentTimeMillis()));
//					添加netty定时器处 
//					AsynBack.timer.newTimeout(new TimeTask(), delay, unit);
//					String sTaskTimeOut = Global.getSingleton().getServiceConfig().getString("back.task.timeout");
//					long delayTime = 1000L;
//					if(sTaskTimeOut != null && !"".equals(sTaskTimeOut)){
//						delayTime = ((Long.parseLong(sTaskTimeOut) * 3)/2) + 1;
//					}
//					AsynBack.timer.newTimeout(new TimeTask(sessionid, System.currentTimeMillis(), delayTime), delayTime, TimeUnit.MILLISECONDS);
				}
				
				//invoker real service
				SCFResponse scfResponse = localProxy.invoke(context);

				if(context.isAsyn()) {
					return;
				}

				sw.stop(swInvoderKey);
				
				
				logger.debug("end localProxy.invoke");
				context.setScfResponse(scfResponse);
				response = createResponse(scfResponse);
				logger.debug("localProxy.invoke finish");
			}
		} catch (ServiceFrameException sfe) {
			logger.error("ServiceFrameException when invoke service fromIP:" + context.getChannel().getRemoteIP() + "  toIP:" + context.getChannel().getLocalIP(), sfe);
			response = ExceptionHelper.createError(sfe);
			context.setError(sfe);
			FrameExCount.messageRecv();
			SCFContext.removeThreadLocal();
		} catch (Throwable e) {
			logger.error("Exception when invoke service fromIP:" + context.getChannel().getRemoteIP() + "  toIP:" + context.getChannel().getLocalIP(), e);
			response = ExceptionHelper.createError(e);
			context.setError(e);
			SCFContext.removeThreadLocal();
		}
		
		protocol.setSdpEntity(response);
		logger.debug("---------------------------------- end --------------------------------");
	}
	
	
	/**
	 * create response message body
	 * @param scfResponse
	 * @return
	 */
	ResponseProtocol createResponse(SCFResponse scfResponse) {
		if(scfResponse.getOutParaList()!= null && scfResponse.getOutParaList().size() > 0){
			int outParaSize = scfResponse.getOutParaList().size();
			Object[] objArray = new Object[outParaSize];
			for(int i=0; i<outParaSize; i++) {
				objArray[i] = scfResponse.getOutParaList().get(i).getOutPara();
			}
            return new ResponseProtocol(scfResponse.getReturnValue(), objArray);
        } else {
            return new ResponseProtocol(scfResponse.getReturnValue(), null);
        }
	}
}