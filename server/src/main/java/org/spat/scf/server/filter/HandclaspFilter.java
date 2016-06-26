package org.spat.scf.server.filter;

import org.spat.scf.protocol.sdp.ExceptionProtocol;
import org.spat.scf.protocol.sdp.HandclaspProtocol;
import org.spat.scf.protocol.enumeration.PlatformType;
import org.spat.scf.protocol.sfp.Protocol;
import org.spat.scf.server.contract.context.ExecFilterType;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.context.SCFResponse;
import org.spat.scf.server.contract.context.SecureContext;
import org.spat.scf.server.contract.context.ServerType;
import org.spat.scf.server.contract.filter.IFilter;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.secure.SecureKey;
import org.spat.scf.server.secure.StringUtils;
import org.spat.scf.server.utility.ExceptionHelper;

public class HandclaspFilter implements IFilter {
	
	private static ILog logger = LogFactory.getLogger(HandclaspFilter.class);
	
	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * 权限认证filter
	 */
	@Override
	public void filter(SCFContext context) throws Exception {
		
		Protocol protocol = context.getScfRequest().getProtocol();		
		if(protocol.getPlatformType() == PlatformType.Java && context.getServerType() == ServerType.TCP){//java 客户端支持权限认证
			SCFResponse response = new SCFResponse();
			Global global = Global.getSingleton();
			//是否启用权限认证
			if(Global.getSingleton().getGlobalSecureIsRights()){
				SecureContext sc = global.getGlobalSecureContext(context.getChannel().getNettyChannel());
				//判断当前channel是否通过认证
				if(!sc.isRights()){
					//没有通过认证
					if(protocol != null && protocol.getSdpEntity() instanceof HandclaspProtocol){
						SecureKey sk = new SecureKey();
						HandclaspProtocol handclaspProtocol = (HandclaspProtocol)protocol.getSdpEntity();
						/**
						 * 接收 客户端公钥
						 */
						if("1".equals(handclaspProtocol.getType())){
							sk.initRSAkey();
							//客户端发送公钥数据
							String clientPublicKey = handclaspProtocol.getData();
							if(null == clientPublicKey || "".equals(clientPublicKey)){
								logger.warn("get client publicKey warn!");
							}
							//java 客户端
							if(protocol.getPlatformType() == PlatformType.Java){
								//服务器生成公/私钥,公钥传送给客户端
								sc.setServerPublicKey(sk.getStringPublicKey());
								sc.setServerPrivateKey(sk.getStringPrivateKey());
								sc.setClientPublicKey(clientPublicKey);
								handclaspProtocol.setData(sk.getStringPublicKey());//服务器端公钥
							}
							//net客户端
							if(protocol.getPlatformType() == PlatformType.Dotnet){
								//服务器生成公/私钥,公钥传送给客户端
	//							String abcd = sk.getStringPrivateKey();
	//							sc.setServerPublicKey(sk.getStringPublicKey());
	//							sc.setServerPrivateKey(sk.getStringPrivateKey());
	//							sc.setClientPublicKey(clientPublicKey);
	//							handclaspProtocol.setData(sk.getStringPublicKey());//服务器端公钥
							}
							
							protocol.setSdpEntity(handclaspProtocol);
							response.setResponseBuffer(protocol.toBytes());
							context.setScfResponse(response);
							this.setInvokeAndFilter(context);
							logger.info("send server publieKey sucess!");
						} 
						/**
						 * 接收权限文件
						 */
						else if("2".equals(handclaspProtocol.getType())){
							//客户端加密授权文件
							String clientSecureInfo = handclaspProtocol.getData();
							if(null == clientSecureInfo || "".equals(clientSecureInfo)){
								logger.warn("get client secureKey warn!");
							}
							//授权文件客户端原文(服务器私钥解密)
							String sourceInfo = sk.decryptByPrivateKey(clientSecureInfo, sc.getServerPrivateKey());
							//校验授权文件是否相同
							//判断是否合法,如果合法服务器端生成DES密钥，通过客户端提供的公钥进行加密传送给客户端
							if(global.containsSecureMap(sourceInfo)){
								logger.info("secureKey is ok!");
								String desKey = StringUtils.getRandomNumAndStr(8);
								//设置当前channel属性
								sc.setDesKey(desKey);
								sc.setRights(true);
								handclaspProtocol.setData(sk.encryptByPublicKey(desKey, sc.getClientPublicKey()));
								protocol.setSdpEntity(handclaspProtocol);
								response.setResponseBuffer(protocol.toBytes());
								context.setScfResponse(response);
							}else{
								logger.error("It's bad secureKey!");
								this.ContextException(context, protocol, response, "授权文件错误!");
							}
							this.setInvokeAndFilter(context);
						}else{
							//权限认证 异常情况
							logger.error("权限认证异常!");
							this.ContextException(context, protocol, response, "权限认证 异常!");
						}
						response = null;
						sk = null;
						handclaspProtocol = null;
					}else{
						//客户端没有启动权限认证
						logger.error("客户端没有启用权限认证!");
						this.ContextException(context, protocol, response, "客户端没有启用权限认证!");
					}
				}
			}else{
				if(protocol != null && protocol.getSdpEntity() instanceof HandclaspProtocol){
					//异常--当前服务器没有启动权限认证
					logger.error("当前服务没有启用权限认证!");
					this.ContextException(context, protocol, response, "当前服务没有启用权限认证!");
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
}
