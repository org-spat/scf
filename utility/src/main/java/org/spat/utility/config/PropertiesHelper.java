package org.spat.utility.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertiesHelper {

	private Properties pro = null;

	public PropertiesHelper(String path) throws Exception {
		pro = loadProperty(path);
	}

	public PropertiesHelper(InputStream inputStream) {
		pro = new Properties();
		try {
			pro.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getString(String key) throws Exception {
		try {
			return pro.getProperty(key);
		} catch (Exception e) {
			throw new Exception("key:" + key);
		}
	}

	public int getInt(String key) throws Exception {
		try {
			return Integer.parseInt(pro.getProperty(key));
		} catch (Exception e) {
			throw new Exception("key:" + key);
		}
	}

	public double getDouble(String key) throws Exception {
		try {
			return Double.parseDouble(pro.getProperty(key));
		} catch (Exception e) {
			throw new Exception("key:" + key);
		}
	}

	public long getLong(String key) throws Exception {
		try {
			return Long.parseLong(pro.getProperty(key));
		} catch (Exception e) {
			throw new Exception("key:" + key);
		}
	}

	public float getFloat(String key) throws Exception {
		try {
			return Float.parseFloat(pro.getProperty(key));
		} catch (Exception e) {
			throw new Exception("key:" + key);
		}
	}
	
	public boolean getBoolean(String key) throws Exception {
		try {
			return Boolean.parseBoolean(pro.getProperty(key));
		} catch (Exception e) {
			throw new Exception("key:" + key);
		}
	}
	
	public Set<Object> getAllKey(){
		return pro.keySet();
	}
	
	public Collection<Object> getAllValue(){
		return pro.values();
	}
	
	public Map<String,Object> getAllKeyValue(){
		Map<String,Object> mapAll = new HashMap<String,Object>();
		Set<Object> keys = getAllKey();
		
		Iterator<Object> it = keys.iterator();
		while(it.hasNext()){
			String key = it.next().toString();
			mapAll.put(key, pro.get(key));
		}
		return mapAll;
	}

	private Properties loadProperty(String filePath) throws Exception {
		FileInputStream fin = null;
		Properties pro = new Properties();
		try {
			fin = new FileInputStream(filePath);
			pro.load(fin);
		} catch (IOException e) {
			throw e;
		} finally {
			if(fin != null) {
				fin.close();
			}
		}
		return pro;
	}
}