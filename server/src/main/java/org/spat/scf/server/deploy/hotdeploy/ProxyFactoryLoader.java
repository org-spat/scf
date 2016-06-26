package org.spat.scf.server.deploy.hotdeploy;

import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.IProxyFactory;
import org.spat.scf.server.deploy.bytecode.CreateManager;

/**
 * A class for dynamic load ProxyHandle
 * 
 * @author Service Platform Architecture Team
 * 
 * 
 */
public class ProxyFactoryLoader {

	/**
	 * 
	 * @param serviceConfig
	 * @return
	 * @throws Exception
	 */
	public static IProxyFactory loadProxyFactory(DynamicClassLoader classLoader) throws Exception {
		
		CreateManager cm = new CreateManager();
		return cm.careteProxy(Global.getSingleton().getRootPath() 
								  + "service/deploy/" 
								  + Global.getSingleton().getServiceConfig().getString("scf.service.name"), 
							  classLoader);
	}
}