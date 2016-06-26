package org.spat.scf.server.bootstrap;
import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.IProxyFactory;
import org.spat.scf.server.contract.context.ServiceConfig;
import org.spat.scf.server.contract.filter.IFilter;
import org.spat.scf.server.contract.init.IInit;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.Log4jConfig;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.contract.log.SystemPrintStream;
import org.spat.scf.server.contract.server.IServer;
import org.spat.scf.server.deploy.filemonitor.FileMonitor;
import org.spat.scf.server.deploy.filemonitor.HotDeployListener;
import org.spat.scf.server.deploy.filemonitor.NotifyCount;
import org.spat.scf.server.deploy.hotdeploy.DynamicClassLoader;
import org.spat.scf.server.deploy.hotdeploy.GlobalClassLoader;
import org.spat.scf.server.deploy.hotdeploy.ProxyFactoryLoader;

import sun.misc.Signal;

/**
 * serive frame entry
 * main para: serviceName
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
@SuppressWarnings("restriction")
public class Main {

	private static ILog logger = null;
	
	/**
	 * start server
	 * @param args : service name
	 * @throws Exception
	 */
	
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			throw new IllegalArgumentException("usage: -Dscf.service.name=<service-name> [<other-scf-config>]");
		}
		

		/*/
		//String userDir = System.getProperty("user.dir");
		/*/
		
		//String userDir = System.getProperty("user.dir");
		String userDir = "d:/scfV3/bin";
		//*/
		String rootPath = userDir + "/../";
		
		System.out.println(rootPath);

		String serviceName = "no service name please set it";
		
		Map<String, String> argsMap = new HashMap<String, String>();
		Global.getSingleton().setRootPath(rootPath);
		
		for(int i=0; i<args.length; i++) {
			if(args[i].startsWith("-D")) {
				String[] aryArg = args[i].split("=");
				if(aryArg.length == 2) {
					if(aryArg[0].equalsIgnoreCase("-Dscf.service.name")) {
						serviceName = aryArg[1];
					}
					argsMap.put(aryArg[0].replaceFirst("-D", ""), aryArg[1]);
				}
			}
		}
		
		String serviceFolderPath = rootPath + "service/deploy/" + serviceName;
		String scfConfigDefaultPath = rootPath + "conf/scf_config.xml";
		String scfConfigPath = serviceFolderPath + "/scf_config.xml";
		String log4jConfigDefaultPath = rootPath + "conf/scf_log4j.xml";
		String log4jConfigPath = serviceFolderPath + "/scf_log4j.xml";
		
		// load log4j2
		loadLog4jConfig(log4jConfigPath, log4jConfigDefaultPath, serviceName);
		
		
		ServiceConfig sc = loadServiceConfig(scfConfigDefaultPath, scfConfigPath);
		
		if(sc == null) {
			throw new Exception("service config is null");
		}
		
		if(sc.getBoolean("asyn.log.switch")) {
			System.setProperty("asyn.log.switch", "true");
			String size = sc.getString("asyn.log.queue.size");
			if(size != null && !"".equals(size)) {
				System.setProperty("asyn.log.queue.size", size);
			}
		}
		
		logger = LogFactory.getLogger(Main.class);
		
		logger.info("+++++++++++++++++++++ staring +++++++++++++++++++++\n");
		
		logger.info("user.dir: " + userDir);
		logger.info("rootPath: " + rootPath);
		logger.info("service scf_config.xml: " + scfConfigPath);
		logger.info("default scf_config.xml: " + scfConfigDefaultPath);
		logger.info("service scf_log4j.xml: " + log4jConfigPath);
		logger.info("default scf_log4j.xml: " + log4jConfigDefaultPath);
		
		
		// load service config
		logger.info("load service config...");
		Set<String> keySet = argsMap.keySet();
		for(String key : keySet) {
			logger.info(key + ": " + argsMap.get(key));
			sc.set(key, argsMap.get(key));
		}
		if(sc.getString("scf.service.name") == null || sc.getString("scf.service.name").equalsIgnoreCase("")) {
			logger.info("scf.service.name:" + serviceName);
			sc.set("scf.service.name", serviceName);
		}
		Global.getSingleton().setServiceConfig(sc);

		
		// init class loader
		logger.info("-----------------loading global jars------------------");
		DynamicClassLoader classLoader = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
		classLoader.addFolder(
				rootPath + "service/deploy/" + sc.getString("scf.service.name") + "/",
				rootPath + "service/lib/",
				rootPath + "lib"
				);
		
		GlobalClassLoader.addSystemClassPathFolder(
				rootPath + "service/deploy/" + sc.getString("scf.service.name") + "/",
				rootPath + "service/lib/",
				rootPath + "lib"
				);
		logger.info("-------------------------end-------------------------\n");

		if(new File(serviceFolderPath).isDirectory() || !serviceName.equalsIgnoreCase("error_service_name_is_null")) {
			// load proxy factory
			logger.info("--------------------loading proxys-------------------");
			Object tem = ProxyFactoryLoader.loadProxyFactory(classLoader);
			IProxyFactory proxyFactory = ProxyFactoryLoader.loadProxyFactory(classLoader);
			Global.getSingleton().setProxyFactory(proxyFactory);
			logger.info("-------------------------end-------------------------\n");
			
			// load init beans
			logger.info("-----------------loading init beans------------------");
			loadInitBeans(classLoader, sc);
			logger.info("-------------------------end-------------------------\n");
		}
		
		// load global request-filters
		logger.info("-----------loading global request filters------------");
		List<IFilter> requestFilters = loadFilters(classLoader, sc, "scf.filter.global.request");
		for(IFilter filter : requestFilters) {
			Global.getSingleton().addGlobalRequestFilter(filter);
		}
		logger.info("-------------------------end-------------------------\n");
		
		// load global response-filters
		logger.info("-----------loading global response filters-----------");
		List<IFilter> responseFilters = loadFilters(classLoader, sc, "scf.filter.global.response");
		for(IFilter filter : responseFilters) {
			Global.getSingleton().addGlobalResponseFilter(filter);
		}
		logger.info("-------------------------end-------------------------\n");
		
		// load connection filters
		logger.info("-----------loading connection filters-----------");
		List<IFilter> connFilters = loadFilters(classLoader, sc, "scf.filter.connection");
		for(IFilter filter : connFilters) {
			Global.getSingleton().addConnectionFilter(filter);
		}
		logger.info("-------------------------end-------------------------\n");
		
		// load secureKey 当scf.secure不为true时不启动权限认证
		logger.info("------------------load secureKey start---------------------");
		if(sc.getString("scf.secure") != null && "true".equalsIgnoreCase(sc.getString("scf.secure"))) {
			logger.info("scf.secure:" + sc.getString("scf.secure"));
			loadSecureKey(sc,serviceFolderPath);
		}
		logger.info("------------------load secureKey end----------------------\n");
		
		//注册信号 linux下支持USR2
		logger.info("------------------signal registr start---------------------");
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName != null && osName.indexOf("window") == -1){
			OperateSignal operateSignalHandler = new OperateSignal();
			Signal sig = new Signal("USR2");
			Signal.handle(sig, operateSignalHandler);
		}
		logger.info("------------------signal registr success----------------------\n");
		
		// load servers
		logger.info("------------------ starting servers -----------------");
		loadServers(classLoader, sc);
		logger.info("-------------------------end-------------------------\n");
		
		// add current service file to monitor
		if(sc.getBoolean("scf.hotdeploy")) {
			logger.info("------------------init file monitor-----------------");
			addFileMonitor(rootPath, sc.getString("scf.service.name"));
			logger.info("-------------------------end-------------------------\n");
		}
		
		try {
			registerExcetEven();
		} catch (Exception e) {
			logger.error("registerExcetEven error", e);
			System.exit(0);
		}
		
		logger.info("+++++++++++++++++++++ server start success!!! +++++++++++++++++++++\n");
		while (true) {
			Thread.sleep(1 * 60 * 60 * 1000);
		}
	}
	
	/**
	 * load service config
	 * @param cps
	 * @return
	 * @throws Exception
	 */
	private static ServiceConfig loadServiceConfig(String... cps) throws Exception {
		ServiceConfig sc = ServiceConfig.getServiceConfig(cps);
		return sc;
	}
	
	/**
	 * 
	 * @param configPath
	 * @param logFilePath
	 * @throws Exception
	 */
	private static void loadLog4jConfig(String configPath, String defaultPath, String serviceName) throws Exception {
		File fLog4jConfig = new File(configPath);
	    if (fLog4jConfig.exists()) {
	      Log4jConfig.configure(configPath);
	      SystemPrintStream.redirectToLog4j();
	    } else {
	      Log4jConfig.configure(defaultPath);

	      SystemPrintStream.redirectToLog4j();
	    }	
	}
	
	/**
	 * 
	 * @param classLoader
	 * @param sc
	 * @throws Exception
	 */
	private static void loadInitBeans(DynamicClassLoader classLoader, ServiceConfig sc) throws Exception{
		List<String> initList = sc.getList("scf.init", ",");
		if(initList != null) {
			for(String initBeans : initList) {
				try {
					logger.info("load: " + initBeans);
					IInit initBean = (IInit)classLoader.loadClass(initBeans).newInstance();
					Global.getSingleton().addInit(initBean);
					initBean.init();
				} catch(Exception e) {
					logger.error("init " + initBeans + " error!!!", e);
				}
			}
		}
	}
	
	/**
	 * 加载授权文件方法
	 * @param sc
	 * @param key
	 * @param serverName
	 * @throws Exception
	 */
	private static void loadSecureKey(ServiceConfig sc, String path) throws Exception{
		File[] file = new File(path).listFiles();
		for(File f : file){	
			String fName = f.getName();
			if(!f.exists() || fName.indexOf("secure") < 0 || !"xml".equalsIgnoreCase(fName.substring(fName.lastIndexOf(".")+1))){
				continue;
			}
			sc.getSecureConfig(f.getPath());
		}
	}
	
	/**
	 * 
	 * @param classLoader
	 * @param sc
	 * @param key
	 * @throws Exception
	 */
	private static List<IFilter> loadFilters(DynamicClassLoader classLoader, ServiceConfig sc, String key) throws Exception {
		List<String> filterList = sc.getList(key, ",");
		List<IFilter> instanceList = new ArrayList<IFilter>();
		if(filterList != null) {
			for(String filterName : filterList) {
				try {
					logger.info("load: " + filterName);
					IFilter filter = (IFilter)classLoader.loadClass(filterName).newInstance();
					instanceList.add(filter);
				} catch(Exception e) {
					logger.error("load " + filterName + " error!!!", e);
				}
			}
		}
		
		return instanceList;
	}
	
	/**
	 * 
	 * @param classLoader
	 * @param sc
	 * @throws Exception
	 */
	private static void loadServers(DynamicClassLoader classLoader, ServiceConfig sc) throws Exception {
		List<String> servers = sc.getList("scf.servers", ",");
		if(servers != null) {
			for(String server : servers) {
				try {
					if(sc.getBoolean(server + ".enable")) {
						logger.info(server + " is starting...");
						IServer serverImpl = (IServer) classLoader.loadClass(sc.getString(server + ".implement")).newInstance();
						Global.getSingleton().addServer(serverImpl);
						serverImpl.start();
						logger.info(server + "started success!!!\n");
					}
				} catch(Exception e) {
					logger.error(server + "start ERROR", e);
				}
			}
		}
	}
	
	/**
	 * add current service file to file monitor
	 * @param config
	 * @throws Exception 
	 */
	private static void addFileMonitor(String rootPath, String serviceName) throws Exception {
		FileMonitor.getInstance().addMonitorFile(rootPath + 
												"service/deploy/" + 
												serviceName + 
												"/");
		
		FileMonitor.getInstance().setInterval(5000);
		FileMonitor.getInstance().setNotifyCount(NotifyCount.Once);
		FileMonitor.getInstance().addListener(new HotDeployListener());
		FileMonitor.getInstance().start();
	}
	
	/**
	 * when shutdown server destroyed all socket connection
	 */
	private static void registerExcetEven() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				for(IServer server : Global.getSingleton().getServerList()) {
					try {
						server.stop();
					} catch (Exception e) {
						logger.error("stop server error", e);
					}
				}

				try {
					super.finalize();
				} catch (Throwable e) {
					logger.error("super.finalize() error when stop server", e);
				}
			}
		});
	}
}
