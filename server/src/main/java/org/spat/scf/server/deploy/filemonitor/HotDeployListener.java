package org.spat.scf.server.deploy.filemonitor;

//import org.spat.scf.serializer.utility.TypeHelper;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.IProxyFactory;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.deploy.hotdeploy.DynamicClassLoader;
import org.spat.scf.server.deploy.hotdeploy.ProxyFactoryLoader;

/**
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class HotDeployListener implements IListener {
	
	/**
	 * log
	 */
	private static ILog logger = LogFactory.getLogger(HotDeployListener.class);

	
	public void fileChanged(FileInfo fInfo) {
		logger.info("service file is change!!! ");
		try {
			logger.info("begin hot deploy scf...");
			
			DynamicClassLoader classLoader = new DynamicClassLoader();
			classLoader.addFolder(
					Global.getSingleton().getRootPath() + "service/deploy/" + Global.getSingleton().getServiceConfig().getString("scf.service.name") + "/",
					Global.getSingleton().getRootPath() + "service/lib/",
					Global.getSingleton().getRootPath() + "lib"
					);
			
			IProxyFactory proxyFactory = ProxyFactoryLoader.loadProxyFactory(classLoader);
			if(proxyFactory != null) {
				Global.getSingleton().setProxyFactory(proxyFactory);
				logger.info("change context class loader");
				Thread.currentThread().setContextClassLoader(proxyFactory.getClass().getClassLoader());
				logger.info("init serializer type map");
				//TypeHelper.InitTypeMap();
				logger.info("notice gc");
				System.gc();
				logger.info("hot deploy service success!!!");
				
			} else {
				logger.error("IInvokerHandle is null when hotDeploy!!!");
			}

			logger.info("finish hot deploy!!!");
		} catch (Exception e) {
			logger.error("create proxy error" , e);
		}
	}
}