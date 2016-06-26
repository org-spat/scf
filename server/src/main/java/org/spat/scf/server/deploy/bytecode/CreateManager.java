package org.spat.scf.server.deploy.bytecode;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.spat.scf.server.contract.context.IProxyFactory;
import org.spat.scf.server.contract.context.IProxyStub;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.deploy.hotdeploy.DynamicClassLoader;

/**
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class CreateManager {
	
	private static ILog logger = LogFactory.getLogger(CreateManager.class);

	public IProxyFactory careteProxy(String serviceRootPath, DynamicClassLoader classLoader) throws Exception {
		ContractInfo serviceContract = ScanClass.getContractInfo(serviceRootPath + "/", classLoader);
		long time = System.currentTimeMillis();
		List<ClassFile> localProxyList = new ProxyClassCreater().createProxy(classLoader, serviceContract, time);
		logger.info("proxy class buffer creater finish!!!");
		ClassFile cfProxyFactory = new ProxyFactoryCreater().createProxy(classLoader, serviceContract, time);
		logger.info("proxy factory buffer creater finish!!!");
		
		List<IProxyStub> localProxyAry = new ArrayList<IProxyStub>();
		for(ClassFile cf : localProxyList) {
			Class<?> cls = classLoader.findClass(cf.getClsName(), cf.getClsByte(), null);
			logger.info("dynamic load class:" + cls.getName());
			localProxyAry.add((IProxyStub)cls.newInstance());
		}
		
		Class<?> proxyFactoryCls = classLoader.findClass(cfProxyFactory.getClsName(), cfProxyFactory.getClsByte(), null);
		Constructor<?> constructor = proxyFactoryCls.getConstructor(List.class);
		IProxyFactory pfInstance = (IProxyFactory)constructor.newInstance(localProxyAry);
		logger.info("crate ProxyFactory instance!!!");
		return pfInstance;
	}
}