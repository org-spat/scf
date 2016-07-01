package org.spat.scf.server.utility;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.spat.scf.server.deploy.hotdeploy.DynamicClassLoader;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class ClassHelper {

	/**
	 * get all class from jar
	 * 
	 * @param jarPath
	 * @param classLoader
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NotFoundException 
	 */
	public static Set<Class<?>> getClassFromJar(String jarPath, DynamicClassLoader classLoader)
			throws IOException, ClassNotFoundException, NotFoundException {
		JarFile jarFile = new JarFile(jarPath);
		Enumeration<JarEntry> entries = jarFile.entries();
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		while (entries.hasMoreElements()) {
			JarEntry jarEntry = entries.nextElement();
			String name = jarEntry.getName();
			if (name.endsWith(".class")) {
				//从.class文件中过滤一遍，降低非必须class的加载		
				if(!CheckClass(jarFile,jarEntry)){
					continue;
				}	
				String className = name.substring(0, name.length() - ".class".length()).replaceAll("/", ".");
				Class<?> cls = null;
				try {
					//cls = classLoader.findClass(className); //注：此处原来为cls = classLoader.loadClass(className)，但是loadClass出来的class的classloader是父classloader，运行异常
					cls = classLoader.loadClass(className);
				} catch (ClassNotFoundException ex) {
					ex.printStackTrace();
				} catch (NoClassDefFoundError e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
				if (cls != null) {
					classes.add(cls);
				}
			}
		}
		return classes;
	}

	/**
	 * check class is implements the interface
	 * 
	 * @param type
	 * @param interfaceType
	 * @return
	 */
	public static boolean interfaceOf(Class<?> type, Class<?> interfaceType) {
		if (type == null) {
			return false;
		}
		Class<?>[] interfaces = type.getInterfaces();
		for (Class<?> c : interfaces) {
			if (c.equals(interfaceType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * get method parameter names
	 * 
	 * @param cls
	 * @param method
	 * @return
	 * @throws Exception
	 */
	public static String[] getParamNames(Class<?> cls, Method method) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.get(cls.getName());

		Class<?>[] paramAry = method.getParameterTypes();
		String[] paramTypeNames = new String[paramAry.length];
		for (int i = 0; i < paramAry.length; i++) {
			paramTypeNames[i] = paramAry[i].getName();
		}

		CtMethod cm = cc.getDeclaredMethod(method.getName(), pool.get(paramTypeNames));

		MethodInfo methodInfo = cm.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		if (codeAttribute == null) {
			return null;
		}
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
		if (attr == null) {
			throw new Exception("class:" + cls.getName()
					+ ", have no LocalVariableTable, please use javac -g:{vars} to compile the source file");
		}
		String[] paramNames = new String[cm.getParameterTypes().length];
		int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
		for (int i = 0; i < paramNames.length; i++) {
			paramNames[i] = attr.variableName(i + pos);
		}
		return paramNames;
	}

	/**
	 * get method parameters annotations
	 * 
	 * @param cls
	 * @param method
	 * @return
	 * @throws Exception
	 */
	public static Object[][] getParamAnnotations(Class<?> cls, Method method) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.get(cls.getName());

		Class<?>[] paramAry = method.getParameterTypes();
		String[] paramTypeNames = new String[paramAry.length];
		for (int i = 0; i < paramAry.length; i++) {
			paramTypeNames[i] = paramAry[i].getName();
		}

		CtMethod cm = cc.getDeclaredMethod(method.getName(), pool.get(paramTypeNames));
		return cm.getParameterAnnotations();
	}

	@SuppressWarnings("unchecked")
	private static Boolean CheckClass(JarFile jarFile, JarEntry entry) throws NotFoundException, IOException {
		Boolean result = false;
		InputStream inputStream = jarFile.getInputStream(entry);
		DataInputStream dataInputStream = new DataInputStream(inputStream);
		try{
			ClassFile cf = new ClassFile(dataInputStream);
			List<AttributeInfo> attrs = cf.getAttributes();
			for(AttributeInfo att : attrs){
				if(att.toString().equals("@org.spat.scf.protocol.annotation.ServiceBehavior")){
					result = true;
					break;
				}else if(att.toString().equals("@org.spat.scf.protocol.annotation.ServiceContract")){
					result = true;
					break;
				}
			}
		}finally{
			inputStream.close();
			dataInputStream.close();
		}
		return result;
	}

}