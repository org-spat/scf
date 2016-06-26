package org.spat.scf.server.filter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.spat.scf.protocol.enumeration.PlatformType;
import org.spat.scf.protocol.sdp.ExceptionProtocol;
import org.spat.scf.protocol.sdp.RequestProtocol;
import org.spat.scf.protocol.sfp.Protocol;
import org.spat.scf.protocol.utility.KeyValuePair;
import org.spat.scf.server.contract.context.ExecFilterType;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.context.SCFResponse;
import org.spat.scf.server.contract.context.SecureContext;
import org.spat.scf.server.contract.context.ServerType;
import org.spat.scf.server.contract.filter.IFilter;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.utility.ExceptionHelper;

public class ExecuteMethodFilter implements IFilter  {
	
	private static ILog logger = LogFactory.getLogger(ExecuteMethodFilter.class);
	
	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void filter(SCFContext context) throws Exception {
		
		Global global = Global.getSingleton();
		Protocol p = context.getScfRequest().getProtocol();
		SCFResponse response = new SCFResponse();
		
		if(p.getPlatformType() == PlatformType.Java && context.getServerType() == ServerType.TCP){ //java 客户端支持权限认证
			//当前服务启动权限认证,并且当前channel通过校验，则进行方法校验
			SecureContext securecontext = global.getGlobalSecureContext(context.getChannel().getNettyChannel());
			if(global.getGlobalSecureIsRights()){
				//当前服务启用权限认证,判断当前channel是否通过授权
				if(securecontext.isRights()){
					RequestProtocol request = (RequestProtocol)p.getSdpEntity();
					if(request != null){
						StringBuffer buff = new StringBuffer(request.getLookup() + "." +request.getMethodName());//接口实现类.方法名(参数序列)
						buff.append("(");
						List<KeyValuePair> list = request.getParaKVList();
						if(list != null){
							int i=0;
							for(KeyValuePair k : list){
								if(k != null){
									if(i > 0){
										buff.append(",");
									}
									buff.append(k.getKey());
									++i;
								}
							}
						}
						buff.append(")");
						
						boolean bool = true;
						Map map = global.getSecureMap();
						if(map != null){
							Iterator<Map.Entry<String, List<String>>> iter = map.entrySet().iterator();
							while(iter.hasNext()){
								Map.Entry<String, List<String>> enty = (Map.Entry<String, List<String>>)iter.next();
								for(String str:enty.getValue()){
									if(str.equalsIgnoreCase(buff.toString())){
										bool = false;
										break;
									}
								}
							}
						}
						
						if(bool){
							logger.error("当前调用方法没有授权!");
							this.ContextException(context, p, response, "当前调用方法没有授权!",global.getGlobalSecureIsRights(),securecontext.getDesKey().getBytes("utf-8"));
						}
					}
				}else{
					logger.error("当前连接没有通过权限认证!");
					this.ContextException(context, p, response, "当前连接没有通过权限认证!");
				}
			}
		}
	}
	
	public void ContextException(SCFContext context,Protocol protocol,SCFResponse response,String message,boolean bool,byte[] key) throws Exception{
		ExceptionProtocol ep = ExceptionHelper.createError(new Exception());
		ep.setErrorMsg(message);
		protocol.setSdpEntity(ep);
		response.setResponseBuffer(protocol.toBytes(bool,key));
		context.setScfResponse(response);
		this.setInvokeAndFilter(context);
	}
	
	public void ContextException(SCFContext context,Protocol protocol,SCFResponse response,String message) throws Exception{
		ExceptionProtocol ep = ExceptionHelper.createError(new Exception());
		ep.setErrorMsg(message);
		protocol.setSdpEntity(ep);
		response.setResponseBuffer(protocol.toBytes());
		context.setScfResponse(response);
		this.setInvokeAndFilter(context);
	}
	
	public void setInvokeAndFilter(SCFContext context){
		context.setExecFilter(ExecFilterType.None);
		context.setDoInvoke(false);
	}
}
