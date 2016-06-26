package org.spat.scf.server.filter;

import org.spat.scf.protocol.enumeration.PlatformType;
import org.spat.scf.protocol.sdp.ResetProtocol;
import org.spat.scf.protocol.sfp.Protocol;
import org.spat.scf.server.contract.context.ExecFilterType;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.context.SCFResponse;
import org.spat.scf.server.contract.context.SecureContext;
import org.spat.scf.server.contract.context.ServerStateType;
import org.spat.scf.server.contract.context.ServerType;
import org.spat.scf.server.contract.filter.IFilter;

/**
 * A filter for parse protocol from byte[]
 * 
 * @author Service Platform Architecture Team
 * 
 * 
 */
public class ProtocolParseFilter implements IFilter {

	@Override
	public void filter(SCFContext context) throws Exception {
		if(context.getServerType() == ServerType.TCP) {
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
			Protocol protocol = Protocol.fromBytes(context.getScfRequest().getRequestBuffer(),global.getGlobalSecureIsRights(),desKeyByte);
			context.getScfRequest().setProtocol(protocol);
			/**
			 * 服务重启直接返回
			 * 备注：暂支持java客户端
			 */	
			if(Global.getSingleton().getServerState() == ServerStateType.Reboot && protocol.getPlatformType() == PlatformType.Java){
				SCFResponse response = new SCFResponse();
				ResetProtocol rp = new ResetProtocol();
				rp.setMsg("This server is reboot!");
				protocol.setSdpEntity(rp);
				response.setResponseBuffer(protocol.toBytes(global.getGlobalSecureIsRights(),desKeyByte));
				context.setScfResponse(response);
				context.setExecFilter(ExecFilterType.None);
				context.setDoInvoke(false);
			}
		}
	}

	@Override
	public int getPriority() {
		return 50;
	}

}
