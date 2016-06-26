
package org.spat.scf.client.proxy.component;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;

/**
 * ProxyStandard
 *
 * @author Service Platform Architecture Team 
 */
public class ProxyStandard implements InvocationHandler, Serializable, IProxyStandard {

	private static final long serialVersionUID = 1L;
	
	private Class<?> interfaceClass;
    private MethodCaller methodCaller;
    private String lookup;
    private ILog logger = LogFactory.getLogger(ProxyStandard.class);
    
    /**
     * @param interfaceClass 接口类
     * @param serviceName 服务名
     * @param lookup 接口实现类
     */
    public ProxyStandard(Class<?> interfaceClass, String serviceName, String lookup) {
    	this.lookup = lookup;
        this.interfaceClass = interfaceClass;
        this.methodCaller = new MethodCaller(serviceName, lookup);
    }
    public ProxyStandard(Class<?> interfaceClass, String serviceName, String lookup, String serVersion) {
    	this.lookup = lookup;
        this.interfaceClass = interfaceClass;
        this.methodCaller = new MethodCaller(serviceName, lookup, serVersion);
    }
    /**
     * args 参数
     * method 方法
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {    
    	long start = System.currentTimeMillis();
        Object obj = methodCaller.doMethodCall(args, method);
        long end = System.currentTimeMillis();
        long total = end - start;
        if (total > 200) {
            logger.warn("interface:"+ interfaceClass.getName() +";class:"+ lookup +";method:" + method.getName() + ";invoke time :" + total);
        }
        return obj;
    }
}
