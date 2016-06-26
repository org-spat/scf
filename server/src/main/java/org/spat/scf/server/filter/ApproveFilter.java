package org.spat.scf.server.filter;

import org.spat.scf.protocol.enumeration.PlatformType;
import org.spat.scf.protocol.sdp.ExceptionProtocol;
import org.spat.scf.protocol.sfp.Protocol;
import org.spat.scf.server.contract.context.ApproveContext;
import org.spat.scf.server.contract.context.ExecFilterType;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.context.SCFResponse;
import org.spat.scf.server.contract.context.ServerType;
import org.spat.scf.server.contract.filter.IFilter;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.utility.ExceptionHelper;

public class ApproveFilter implements IFilter{
	private static ILog logger = LogFactory.getLogger(ApproveFilter.class);

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void filter(SCFContext context) throws Exception {
		Protocol protocol = context.getScfRequest().getProtocol();		
		if(protocol.getPlatformType() == PlatformType.Java && context.getServerType() == ServerType.TCP){//java 客户端支持权限认证
			SCFResponse response = new SCFResponse();
			Global global = Global.getSingleton();
			//是否启用权限认证
			if(Global.getSingleton().getApproveIsRights()){
				ApproveContext ac = global.getGlobalAppvoreContext(context.getChannel().getNettyChannel());
				if(ac != null && !ac.isRight()) {
					try {
						String ap = (String)protocol.getSdpEntity();
						if(ap.equalsIgnoreCase(ac.getData())) {
							ac.setRight(true);
							ap = global.getServiceConfig().getString("scf.service.name");
							protocol.setSdpEntity(ap);
							response.setResponseBuffer(protocol.toBytes());
							context.setScfResponse(response);
							this.setInvokeAndFilter(context);
						}else {
							logger.error("It's bad secureKey!");
							this.ContextException(context, protocol, response, "授权字符串错误!");
						}
					}catch(ClassCastException ce) {
						if(global.getServiceConfig().getBoolean("scf.server.approve.compatible")) {
							logger.info("此链接没有通过认证连接服务, IP:" + context.getChannel().getRemoteIP());
							ac.setRight(true);
							context.setDoInvoke(true);
						}else {
							logger.error("approve error!");
							this.ContextException(context, protocol, response, "服务端需要授权才能访问!");
						}
					}catch(Exception e) {
						logger.error("approve error!");
						this.ContextException(context, protocol, response, "服务端需要授权才能访问!");
					}
				}
			}else {
				if(protocol != null && protocol.getSdpEntity() instanceof String){
					//异常--当前服务器没有启动权限认证
					logger.error("当前服务没有启用权限认证!");
					this.ContextException(context, protocol, response, "当前服务没有启用权限认证，请关闭客户端认证配置！");
				}
			}
		}
		
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
	public static void main(String[] args) {
		System.out.println(Math.abs(-2147483648));
	}
}
