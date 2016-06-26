
package org.spat.scf.client.proxy.component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;
import org.spat.scf.client.proxy.ServiceProxy;
import org.spat.scf.protocol.annotation.AnnotationUtil;
import org.spat.scf.protocol.annotation.OperationContract;
import org.spat.scf.protocol.entity.Out;

/**
 * MethodCaller
 *
 * @author Service Platform Architecture Team 
 */
public class MethodCaller {

    private String serviceName;
    private String lookup;
    private static ILog logger = LogFactory.getLogger(MethodCaller.class);
    
    private String serVersion = "SCF";
    
    public MethodCaller(String serviceName, String lookup) {
        this.serviceName = serviceName;
        this.lookup = lookup;
    }
    
    public MethodCaller(String serviceName, String lookup, String serVersion) {
        this.serviceName = serviceName;
        this.lookup = lookup;
        this.serVersion = serVersion;
    }
    
    public Object doMethodCall(Object[] args, Method methodInfo) throws Exception, Throwable {
        Type[] typeAry = methodInfo.getGenericParameterTypes();//ex:java.util.Map<java.lang.String, java.lang.String>
        Class<?>[] clsAry = methodInfo.getParameterTypes();//ex:java.util.Map
        if (args == null) {
            args = new Object[0];
        }
        if (args.length != typeAry.length) {
            throw new Exception("argument count error!");
        }

        ServiceProxy proxy = ServiceProxy.getProxy(serviceName);
        Parameter[] paras = null;
        List<Integer> outParas = new ArrayList<Integer>();
        
        boolean syn = true;
        ReceiveHandler receiveHandler = null;
        int parasLength = 0;
        
        if (typeAry != null) {
        	if((typeAry.length >= 1) && (args[typeAry.length - 1] instanceof ReceiveHandler)){
				syn = false;
				receiveHandler = (ReceiveHandler)args[typeAry.length - 1];
				parasLength = typeAry.length - 1;
				
			}else {
				parasLength = typeAry.length;
            }
        	paras = new Parameter[parasLength];
			for (int i = 0; i < parasLength; i++) {
				 if (args[i] instanceof Out) {
                    paras[i] = new Parameter(args[i], clsAry[i], typeAry[i], ParaType.Out);
                    outParas.add(i);
	                } else {
	                	paras[i] = new Parameter(args[i], clsAry[i], typeAry[i], ParaType.In);
		            }
			}
        }
        
        Parameter returnPara = new Parameter(null, methodInfo.getReturnType(), methodInfo.getGenericReturnType());
        String methodName = methodInfo.getName();
        OperationContract ann = methodInfo.getAnnotation(OperationContract.class);
        if (ann != null) {
            if (!ann.methodName().equals(AnnotationUtil.DEFAULT_VALUE)) {
                methodName = "$" + ann.methodName();
            }
        }
        if (syn) {
        	InvokeResult result = null;
        	if(serVersion.equalsIgnoreCase("SCFV2")) {
        		result = proxy.invoke(returnPara, lookup, methodName, paras, serVersion);
        	}else {
        		result = proxy.invoke(returnPara, lookup, methodName, paras);
        	}
        	 if (result != null && result.getOutPara() != null) {
                 for (int i = 0; i < outParas.size() && i < result.getOutPara().length; i++) {
                     Object op = args[outParas.get(i)];
                     if(op instanceof Out){
                         ((Out)op).setOutPara(result.getOutPara()[i]);
                     }
                 }
             }
             return result.getResult();
        }else {
        	proxy.invoke(returnPara, lookup, methodName, paras, receiveHandler);
        	return null;
        }      
    }
}
