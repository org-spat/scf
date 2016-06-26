package org.spat.utility.tools;

import java.util.Map;

import org.spat.utility.config.PropertiesHelper;

public class UnityFactory {
	
	private static Map<String,Object> mapConfig = null;
	
	private UnityFactory(String configPath) {
		try {
			PropertiesHelper ph = new PropertiesHelper(configPath);
			mapConfig = ph.getAllKeyValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Object lockHelper = new Object();
	private static UnityFactory unityFactory = null;
	
	public static UnityFactory getIntrance(String configPath) {
		if(unityFactory == null){
			synchronized(lockHelper){
				if(unityFactory == null){
					System.out.println("UnityFactory:"+configPath);
					unityFactory = new UnityFactory(configPath);
				}
			}
		}
		return unityFactory;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T create(Class<?> clazz) throws Exception {
		String value = mapConfig.get(clazz.getName()).toString();
		System.out.println("create:"+value);
		return (T)Class.forName(value).newInstance();
	}
}