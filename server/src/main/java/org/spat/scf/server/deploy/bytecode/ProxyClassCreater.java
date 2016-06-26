package org.spat.scf.server.deploy.bytecode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

import org.spat.scf.protocol.annotation.OperationAsyn;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.deploy.bytecode.ClassInfo.MethodInfo;
import org.spat.scf.server.deploy.hotdeploy.DynamicClassLoader;
import org.spat.scf.server.proxy.AsynBack;
import org.spat.scf.server.utility.Util;

/**
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class ProxyClassCreater {
	
	private static ILog logger = LogFactory.getLogger(ProxyClassCreater.class);
	
	@SuppressWarnings("rawtypes")
	public List<ClassFile> createProxy(DynamicClassLoader classLoader, ContractInfo serviceContract, long time) throws Exception {
		
		logger.info("loading dynamic proxy ...");
		List<ClassFile> clsList = new ArrayList<ClassFile>();
		
		for (ContractInfo.SessionBean sessionBean : serviceContract.getSessionBeanList()) {
			if(sessionBean.getInterfaceClass() != null) {
				Iterator it = sessionBean.getInstanceMap().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					String lookup = entry.getKey().toString();
					String implClassName = entry.getValue().toString();
					
					String proxyClassName = lookup + "ProxyStub" + time;
					logger.info("loading => " + proxyClassName);
					logger.info("class name:" + implClassName);
					
					ClassPool pool = ClassPool.getDefault();
					
					List<String> jarList = classLoader.getJarList();
					for(String jar : jarList) {
						pool.appendClassPath(jar);
					}

					CtClass ctProxyClass = pool.makeClass(proxyClassName, null);
					
					CtClass localProxy = pool.getCtClass(Constant.IPROXYSTUB_CLASS_NAME);
					ctProxyClass.addInterface(localProxy);
					
					
					CtField proxyField = CtField.make("private static " +sessionBean.getInterfaceName() +" serviceProxy = new " +implClassName +"();", ctProxyClass);
					ctProxyClass.addField(proxyField);
					
					List<MethodInfo> methodList = sessionBean.getInterfaceClass().getMethodList();
					Method[] methodAry = new Method[methodList.size()];
					for(int i=0; i<methodList.size(); i++) {
						methodAry[i] = methodList.get(i).getMethod();
					}
					
					List<String> uniqueNameList = new ArrayList<String>();
					List<Method> uniqueMethodList = new ArrayList<Method>();
					List<Method> allMethodList = new ArrayList<Method>();
					for (Method m : methodAry) {
						if(Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers())){
							/**依据注释获取是否使用异步方法*/
							 OperationAsyn oa = m.getAnnotation(OperationAsyn.class);
							 if(oa != null) {
								 StringBuffer sb = new StringBuffer();
								 sb.append(lookup);
								 sb.append(m.getName());
								 sb.append(getParas(m));
								 AsynBack.asynMap.put(sb.toString(), 1);
								 logger.info("asynBack asynMap's key :" + sb.toString());
							 }						
							if(!uniqueNameList.contains(m.getName())){
								uniqueNameList.add(m.getName());
								uniqueMethodList.add(m);
							}
							allMethodList.add(m);
						}
					}
	
					//method
					for(Method m : uniqueMethodList) {
						logger.debug("create method:" + m.getName());
						String methodStr = createMethods(proxyClassName, m.getName(), allMethodList, uniqueNameList);
						logger.debug("method("+m.getName()+") source code:"+methodStr);
						CtMethod methodItem = CtMethod.make(methodStr, ctProxyClass);
						ctProxyClass.addMethod(methodItem);
					}					
					
					//invoke
					String invokeMethod = createInvoke(proxyClassName, uniqueNameList);
					logger.debug("create invoke method:" + invokeMethod);
					CtMethod invoke = CtMethod.make(invokeMethod, ctProxyClass);
					ctProxyClass.addMethod(invoke);
	
					clsList.add(new ClassFile(proxyClassName, ctProxyClass.toBytecode()));
				}
			}
		}
		logger.info("load dynamic proxy success!!!");
		return clsList;
	}
	

	/**
	 * ceate invoke method
	 * @param uniqueNameList
	 * @return
	 */
	private String createInvoke(String className, List<String> uniqueNameList) {
		StringBuilder sb = new StringBuilder();
		sb.append("public " + Constant.SCFRESPONSE_CLASS_NAME + " invoke(" + Constant.SCFCONTEXT_CLASS_NAME + " context) throws " + Constant.SERVICEFRAMEEXCEPTION_CLASS_NAME + " {");
		sb.append("String methodName = ((" + Constant.REQUEST_PROTOCOL_CLASS_NAME + ")context.getScfRequest().getProtocol().getSdpEntity()).getMethodName();");
		for (String methodName : uniqueNameList) {
			sb.append("if(methodName.equalsIgnoreCase(\"");
			sb.append(methodName);
			sb.append("\")){return ");
			sb.append(methodName);
			sb.append("(context);}");
		}
		sb.append("throw new " + Constant.SERVICEFRAMEEXCEPTION_CLASS_NAME + "(\"method:" + className + ".invoke--msg:not found method (\"+methodName+\")\", context.getChannel().getRemoteIP(), context.getChannel().getLocalIP(), context.getScfRequest().getProtocol().getSdpEntity(), " + Constant.ERRORSTATE_CLASS_NAME + ".NotFoundMethodException, null);");
		sb.append("}");
		return sb.toString();
	}
	
	
	/**
	 * create service method
	 * @param methodName
	 * @param methodList
	 * @param uniqueNameList
	 * @return
	 */
	public String createMethods(String className, String methodName, List<Method> methodList, List<String> uniqueNameList) {
		StringBuilder sb = new StringBuilder();
		sb.append("public " + Constant.SCFRESPONSE_CLASS_NAME + " ");
		sb.append(methodName);
		sb.append("(" + Constant.SCFCONTEXT_CLASS_NAME + " context) throws " + Constant.SERVICEFRAMEEXCEPTION_CLASS_NAME + " {");
		
		sb.append(Constant.ICONVERT_CLASS_NAME + " convert = " + Constant.CONVERT_FACTORY_CLASS_NAME + ".getConvert(context.getScfRequest().getProtocol());");
		sb.append(Constant.REQUEST_PROTOCOL_CLASS_NAME + " request = (" + Constant.REQUEST_PROTOCOL_CLASS_NAME + ")context.getScfRequest().getProtocol().getSdpEntity();");
		sb.append("java.util.List listKV = request.getParaKVList();");
		
		for(Method m : methodList){
			if(m.getName().equalsIgnoreCase(methodName)){
				
				Class<?>[] mType = m.getParameterTypes();
				Type[] mGenericType = m.getGenericParameterTypes();

				//if
				sb.append("if(listKV.size() == " + mGenericType.length);
				for(int i=0; i<mGenericType.length; i++) {
					String paraName = mGenericType[i].toString().replaceFirst("class ", "");
					paraName = paraName.replaceAll("java.util.", "").replaceAll("java.lang.", "");
					if(paraName.startsWith(Constant.OUT_PARAM)){
						paraName = paraName.replaceAll(Constant.OUT_PARAM+"<", "");
						paraName = paraName.substring(0, paraName.length() - 1);
						paraName = paraName.replaceAll("\\<.*\\>", "");
					}
					if(paraName.startsWith("[")) {
						paraName = mType[i].getCanonicalName();
					}

					paraName = Util.getSimpleParaName(paraName);
					
//					新添加

					if (paraName.indexOf("<[") !=-1 || paraName.indexOf(";>") !=-1 ) {
						paraName = paraName.replaceAll("\\[L", "").replaceAll("\\[", "");
					}
					
//					兼容jdk1.6和jdk1.7
					if (paraName.indexOf("[]>") !=-1 || paraName.indexOf("[],") !=-1 ) {
						paraName = paraName.replace("[]>", ";>").replace("[],", ";,");
					}
					
					sb.append(" && (");
					sb.append("((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get(");
					sb.append(i);
					sb.append(")).getKey().toString().equalsIgnoreCase(\"");
					sb.append(paraName);
					sb.append("\")");

					if(paraName.indexOf("int")>=0) {
						sb.append("|| ((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get(");
						sb.append(i);
						sb.append(")).getKey().toString().equalsIgnoreCase(\"" + paraName.replaceAll("int", "Integer") + "\")");
					} else if(paraName.indexOf("Integer")>=0) {
						sb.append("|| ((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get(");
						sb.append(i);
						sb.append(")).getKey().toString().equalsIgnoreCase(\"" + paraName.replaceAll("Integer", "int") + "\")");
					} else if(paraName.indexOf("char")>=0) {
						sb.append("|| ((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get(");
						sb.append(i);
						sb.append(")).getKey().toString().equalsIgnoreCase(\"" + paraName.replaceAll("char", "Character") + "\")");
					} else if(paraName.indexOf("Character")>=0) {
						sb.append("|| ((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get(");
						sb.append(i);
						sb.append(")).getKey().toString().equalsIgnoreCase(\"" + paraName.replaceAll("Character", "char") + "\")");
					}
					
					
					sb.append(")");
					
				}
				sb.append("){");
				
				sb.append("java.util.List listOutPara = new java.util.ArrayList();");

				//define para
				for(int i=0; i<mGenericType.length;i++){
					String paraName = mGenericType[i].toString().replaceFirst("class ", "");
					boolean isOutPara = false;
					if(paraName.startsWith(Constant.OUT_PARAM)){
						isOutPara = true;
					}
					
					if(paraName.startsWith("[")){
						paraName = mType[i].getCanonicalName();
					}
					if(isOutPara){
						sb.append(Constant.OUT_PARAM + " arg" + i);
						sb.append("= new " + Constant.OUT_PARAM);
						sb.append("();");
					} else {
						sb.append(paraName.replaceAll("\\<.*\\>", ""));
						sb.append(" arg" + i);
					}
					
					if(!isOutPara) {
						paraName = paraName.replaceAll("java.util.", "").replaceAll("java.lang.", "");
						
						if(paraName.equals("long")){
							sb.append(" = 0L;");
						} else if(paraName.equals("float")){
							sb.append(" = 0F;");
						} else if(paraName.equals("double")){
							sb.append(" = 0D;");
						} else if(paraName.equals("int")){
							sb.append(" = 0;");
						} else if(paraName.equals("short")){
							sb.append(" = (short)0;");
						} else if(paraName.equals("byte")){
							sb.append(" = (byte)0;");
						} else if(paraName.equals("boolean")){
							sb.append(" = false;");
						} else if(paraName.equals("char")){
							sb.append(" = (char)'\\0';");
						}
						
						else if(paraName.equals("Long")){
							sb.append(" = new Long(\"0\");");
						} else if(paraName.equals("Float")){
							sb.append(" = new Float(\"0\");");
						} else if(paraName.equals("Double")){
							sb.append(" = new Double(\"0\");");
						} else if(paraName.equals("Integer")){
							sb.append(" = new Integer(\"0\");");
						} else if(paraName.equals("Short")){
							sb.append(" = new Short(\"0\");");
						} else if(paraName.equals("Byte")){
							sb.append(" = new Byte(\"0\");");
						} else if(paraName.equals("Boolean")){
							sb.append(" = new Boolean(\"false\");");
						} else if(paraName.equals("Character")){
							sb.append(" = new Character((char)'\\0');");
						} else{
							sb.append(" = null;");
						}
					}
					if(isOutPara){
						sb.append("listOutPara.add(arg"+i+");");
					}
				}
				
				//set value to para
				if(mGenericType.length > 0) {
					sb.append("try {");
				}
				for(int i=0; i<mGenericType.length;i++){
					String paraName = mGenericType[i].toString().replaceFirst("class ", "");
					boolean isOutPara = false;
					if(paraName.startsWith(Constant.OUT_PARAM)){
						isOutPara = true;
					}
					
					if(paraName.startsWith("[")){
						paraName = mType[i].getCanonicalName();
					}
					
					String pn = paraName.replaceAll("java.util.", "").replaceAll("java.lang.", "");
					if(!isOutPara){
						if (pn.equalsIgnoreCase("String")
								|| pn.equalsIgnoreCase("int") || pn.equalsIgnoreCase("Integer")
								|| pn.equalsIgnoreCase("long")
								|| pn.equalsIgnoreCase("short")
								|| pn.equalsIgnoreCase("float")
								|| pn.equalsIgnoreCase("boolean")
								|| pn.equalsIgnoreCase("double")
								|| pn.equalsIgnoreCase("char") || pn.equalsIgnoreCase("Character")
								|| pn.equalsIgnoreCase("byte")){
							
							sb.append("arg"+i);
							sb.append(" = convert.convertTo"+pn+"(((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get("+i+")).getValue());");
						} else {
							sb.append("arg"+i);
							sb.append(" = ("+paraName.replaceAll("\\<.*\\>", "")+")convert.convertToT(((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get("+i+")).getValue(), \"" + paraName + "\");");
						}
					} else {
						String outType = paraName.replaceAll(Constant.OUT_PARAM + "<", "");
						outType = outType.substring(0, outType.length() - 1);
						
						String outpn = outType.replaceAll("java.util.", "")
						   					  .replaceAll("java.lang.", "");
						
						if (outpn.equalsIgnoreCase("String")
								|| outpn.equalsIgnoreCase("Integer")
								|| outpn.equalsIgnoreCase("Long")
								|| outpn.equalsIgnoreCase("Short")
								|| outpn.equalsIgnoreCase("Float")
								|| outpn.equalsIgnoreCase("Boolean")
								|| outpn.equalsIgnoreCase("Double")
								|| outpn.equalsIgnoreCase("Character")
								|| outpn.equalsIgnoreCase("Byte")){
							
							sb.append("arg"+i);
							sb.append(".setOutPara(convert.convertTo"+outpn+"(((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get("+i+")).getValue()));");
						} else {
							sb.append("arg"+i);
							sb.append(".setOutPara(("+outType+")convert.convertToT(((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get("+i+")).getValue(), \"" + outType + "\")");
							sb.append(");");
						}
					}
				}
				
				if(mGenericType.length > 0) {
					sb.append("} catch (Exception e) {");
					sb.append("throw new " + Constant.SERVICEFRAMEEXCEPTION_CLASS_NAME + "(\"method:" + className + "." + methodName + "--msg:parse scfRequest error\", context.getChannel().getRemoteIP(), context.getChannel().getLocalIP(), context.getScfRequest().getProtocol().getSdpEntity(), " + Constant.ERRORSTATE_CLASS_NAME + ".ParaException, e);");
					sb.append("}");
				}
				
				//define returnValue
				Class<?> classReturn = m.getReturnType();
				Type typeReturn = m.getGenericReturnType();
				String returnValueType = typeReturn.toString().replaceFirst("class ", "");
				if(returnValueType.startsWith("[")){
					returnValueType = classReturn.getCanonicalName();
				}
				 
				sb.append("try {");	
				if(!returnValueType.equalsIgnoreCase("void")) {
					sb.append(returnValueType.replaceAll("\\<.*\\>", "") + " returnValue = ");
				} 

				sb.append("serviceProxy.");
				sb.append(m.getName());
				sb.append("(");
				//method para
				for(int i=0; i<mGenericType.length;i++) {
					sb.append("arg");
					sb.append(i);
					if(i != mGenericType.length-1) {
						sb.append(", ");
					}
				}
				sb.append(");");
				
				if(!returnValueType.equalsIgnoreCase("void")) {
					sb.append("return new " + Constant.SCFRESPONSE_CLASS_NAME + "(returnValue");
				} else {
					sb.append("return new " + Constant.SCFRESPONSE_CLASS_NAME + "(null");
				}
				
				
				sb.append(", listOutPara);");
				sb.append("} catch (Exception e) {");
				sb.append("throw new " + Constant.SERVICEFRAMEEXCEPTION_CLASS_NAME + "(\"method:" + className + "." + methodName + "--msg:invoke real service error\", context.getChannel().getRemoteIP(), context.getChannel().getLocalIP(), context.getScfRequest().getProtocol().getSdpEntity(), " + Constant.ERRORSTATE_CLASS_NAME + ".ServiceException, e);");
				sb.append("}");

				sb.append("}");
				//end if
			}
		}
		sb.append("throw new " + Constant.SERVICEFRAMEEXCEPTION_CLASS_NAME + "(\"method:" + className + "." + methodName + "--msg:not fond method error\", context.getChannel().getRemoteIP(), context.getChannel().getLocalIP(), context.getScfRequest().getProtocol().getSdpEntity(), " + Constant.ERRORSTATE_CLASS_NAME + ".NotFoundMethodException, null);");
		sb.append("}");
		return sb.toString();
	}
	
	private String getParas(Method method) {
		if(method == null) {
			return null;
		}
		Type[] typeAry = method.getGenericParameterTypes();
		@SuppressWarnings("rawtypes")
		Class[] clazz = method.getParameterTypes();
		String sn = "";
		StringBuffer strBuff = new StringBuffer();
			for (int i = 0; i < clazz.length; i++) {
				String itemName = typeAry[i].toString().replaceAll(clazz[i].getCanonicalName().replaceAll("\\[", "").replaceAll("\\]", ""),"").replaceAll("\\<", "").replaceAll("\\>", "");

				if(typeAry[i].toString().lastIndexOf(">") == -1) {
					sn = clazz[i].getSimpleName();
				}else {					
					sn = clazz[i].getCanonicalName();
					sn = sn.substring(sn.lastIndexOf(".") + 1);

					if (itemName.indexOf(",") == -1) {
						itemName = itemName.substring(itemName.lastIndexOf(".") + 1);
						sn = sn + "<" + itemName + ">";
					} else {
						String[] genericItem = typeAry[i].toString().replaceAll(clazz[i].getCanonicalName(),"").replaceAll("\\<", "").replaceAll("\\>", "").split(",");
						sn = sn + "<" + genericItem[0].substring(genericItem[0].lastIndexOf(".") + 1) + "," + genericItem[1].substring(genericItem[1].lastIndexOf(".") + 1) + ">";
					}
				}
				strBuff.append(sn);
			}
			return strBuff.toString();
	}

}