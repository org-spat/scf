package org.spat.scf.server.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.spat.scf.protocol.exception.TimeoutException;
import org.spat.scf.protocol.sdp.ResponseProtocol;
import org.spat.scf.protocol.sfp.Protocol;
import org.spat.scf.server.contract.context.ExecFilterType;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.context.SCFResponse;
import org.spat.scf.server.contract.context.SecureContext;
import org.spat.scf.server.contract.context.StopWatch;
import org.spat.scf.server.contract.filter.IFilter;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.performance.monitor.AbandonCount;
import org.spat.scf.server.utility.ExceptionHelper;
import org.spat.utility.expandasync.AsyncInvoker;
import org.spat.utility.expandasync.IAsyncHandler;

public class AsynBack {
	
	private final static ILog logger = LogFactory.getLogger(AsynBack.class);
	private static AsynBack asyn = null;
	private static int taskTimeOut = 1000;
	private static int inQueueTime = -1;
	public static Map<String, Integer> asynMap = new ConcurrentHashMap<String, Integer>();
	public static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
	public static Map<Integer, SCFContext> contextMap= new ConcurrentHashMap<Integer, SCFContext>();
	public static Map<Integer, String> swInvokeKeyMap = new ConcurrentHashMap<Integer, String>();
	public static final CallBackUtil callBackUtil = new CallBackUtil();
	private static AsyncInvoker asyncInvoker;
	
	static {
		try {
			boolean isSteal = Global.getSingleton().getServiceConfig().getBoolean("scf.async.worker.steal");//读取不到 返回false
			int limitSize = Global.getSingleton().getServiceConfig().getInt("scf.async.worker.limitsize");//读取不到 返回0
			boolean mode = Global.getSingleton().getServiceConfig().getBoolean("scf.async.worker.mode");
			asyncInvoker = AsyncInvoker.getInstance(THREAD_COUNT, limitSize, isSteal, mode, "Back Async Worker");
			String sTaskTimeOut = Global.getSingleton().getServiceConfig().getString("back.task.timeout");
			if(sTaskTimeOut != null && !"".equals(sTaskTimeOut)){
				taskTimeOut = Integer.parseInt(sTaskTimeOut);
			}
			
			String sInQueueTime = Global.getSingleton().getServiceConfig().getString("scf.server.task.asyn.inqueue");
			if(sInQueueTime != null && !"".equals(sInQueueTime)) {
				inQueueTime = Integer.parseInt(sInQueueTime);
			}
			logger.info("back async worker count:" + THREAD_COUNT);
		} catch (Exception e) {
			logger.error("init AsyncInvokerHandle error", e);
		}
	}
	
	private AsynBack(){
		
	}
	
	public static AsynBack getAsynBack() {	
		return asyn != null ? asyn : new AsynBack();
	}
	
	public static void send(final int key, final Object obj) {
		
		final SCFContext context = contextMap.get(key);
		final String swKey = swInvokeKeyMap.get(key);
		if(null == context){
			return;
		}
		synchronized(context){
			if(null == context || context.isDel()){
				return;
			}
			context.setDel(true);
		}
		asyncInvoker.run(taskTimeOut, inQueueTime, new IAsyncHandler() {
			@Override
			public Object run() throws Throwable {
				if(obj instanceof Exception) {
					exceptionCaught((Throwable)obj);
					return null;
				}
				Protocol protocol = context.getScfRequest().getProtocol();
				StopWatch sw = context.getStopWatch();
				sw.stop(swKey);
				SCFResponse response = new SCFResponse(obj, null);
				
				protocol.setSdpEntity(new ResponseProtocol(response.getReturnValue(), null));
				
				for(IFilter f : Global.getSingleton().getGlobalResponseFilterList()) {
					if(context.getExecFilter() == ExecFilterType.All || context.getExecFilter() == ExecFilterType.ResponseOnly) {
						f.filter(context);					
					}
				}
				return context;
			}
			
			@Override
			public void messageReceived(Object obj) {
				if(obj != null) {			
					SCFContext ctx = (SCFContext)obj;
					if(ctx.isAsyn()) {
						ctx.getServerHandler().writeResponse(ctx);
						//contextMap.remove(key);//使用完删除context
					}else {
						logger.error("The Method is Synchronized!");
					}
				}				
			}
			
			@Override
			public void exceptionCaught(Throwable e) {
				String message = "";
				try {
					if(context == null) {
						return;
					}
					
					if(context.getScfResponse() == null){
						SCFResponse respone = new SCFResponse();
						context.setScfResponse(respone);
					}
					
					//任务超时计数
					if(e instanceof TimeoutException){
						try{
							AbandonCount.messageRecv();
						}catch(Exception ex) {
							ex.printStackTrace();
						}
					}
					
					byte[] desKeyByte = null;
					String desKeyStr = null;
					boolean bool = false;
					
					Global global = Global.getSingleton();
					if(global != null){
						//判断当前服务启用权限认证
						if(global.getGlobalSecureIsRights()){
							SecureContext securecontext = global.getGlobalSecureContext(context.getChannel().getNettyChannel());
							bool = securecontext.isRights();
							if(bool){
								desKeyStr = securecontext.getDesKey();
							}
						}
					}
					
					if(desKeyStr != null){
						desKeyByte = desKeyStr.getBytes("utf-8");
					}
					
					Protocol protocol = context.getScfRequest().getProtocol();
					if(protocol == null){
						protocol = Protocol.fromBytes(context.getScfRequest().getRequestBuffer(),global.getGlobalSecureIsRights(),desKeyByte);
						context.getScfRequest().setProtocol(protocol);
					}
					if (e.getMessage().contains("async task timeout")) {
                    	message = ExceptionHelper.createErrorMessage(e, context);
                    }
					
					protocol.setSdpEntity(ExceptionHelper.createError(e));
					context.getScfResponse().setResponseBuffer(protocol.toBytes(Global.getSingleton().getGlobalSecureIsRights(),desKeyByte));
				} catch (Exception ex) {
					context.getScfResponse().setResponseBuffer(new byte[]{0});
					logger.error("AsyncInvokerHandle invoke-exceptionCaught error", ex);
				}finally{
					context.getServerHandler().writeResponse(context);
					logger.error("AsyncBack invoke error" + message, e);
				}
			}
		});
	}
	
}
