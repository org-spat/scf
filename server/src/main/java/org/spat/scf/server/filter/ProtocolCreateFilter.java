package org.spat.scf.server.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.spat.scf.protocol.sfp.Protocol;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.context.SCFResponse;
import org.spat.scf.server.contract.context.SecureContext;
import org.spat.scf.server.contract.context.ServerType;
import org.spat.scf.server.contract.filter.IFilter;

/**
 * A filter for create protocol from byte[]
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class ProtocolCreateFilter implements IFilter {

	private static final Log logger = LogFactory.getLog(ProtocolCreateFilter.class);

	@Override
	public void filter(SCFContext context) throws Exception {
		try {
			if (context.getServerType() == ServerType.TCP) {
				Protocol protocol = context.getScfRequest().getProtocol();
				byte[] desKeyByte = null;
				String desKeyStr = null;
				boolean bool = false;

				Global global = Global.getSingleton();
				if (global != null) {
					// 判断当前服务启用权限认证
					if (global.getGlobalSecureIsRights()) {
						SecureContext securecontext = global
								.getGlobalSecureContext(context.getChannel()
										.getNettyChannel());
						bool = securecontext.isRights();
						if (bool) {
							desKeyStr = securecontext.getDesKey();
						}
					}
				}

				if (desKeyStr != null) {
					desKeyByte = desKeyStr.getBytes("utf-8");
				}

				if (context.getScfResponse() == null) {
					SCFResponse respone = new SCFResponse();
					context.setScfResponse(respone);
				}
				context.getScfResponse().setResponseBuffer(protocol.toBytes(Global.getSingleton().getGlobalSecureIsRights(), desKeyByte));
			}
		} catch (Exception ex) {
			System.out.println(context);
			logger.error("Server ProtocolCreateFilter error!", ex);
		}
	}

	@Override
	public int getPriority() {
		return 50;
	}

}