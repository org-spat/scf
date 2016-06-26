package org.spat.scf.server.utility;

import java.util.regex.Pattern;

import org.spat.scf.server.contract.context.Global;

public class IPTable {
	
	private static Pattern allowPattern;
	private static Pattern forbidPattern;
	
	static {
		init();
	}
	
	public static void init() {
		String allowIP = Global.getSingleton().getServiceConfig().getString("scf.iptable.allow.iplist");
		String forbidIP = Global.getSingleton().getServiceConfig().getString("scf.iptable.forbid.iplist");
		allowIP = allowIP.replaceAll("\\.", "\\\\.")
				   .replaceAll(",", "|")
				   .replaceAll("\\*", "\\.\\*");
		
		forbidIP = forbidIP.replaceAll("\\.", "\\\\.")
		  		   .replaceAll(",", "|")
		  		   .replaceAll("\\*", "\\.\\*");
		
		if(allowIP != null && !allowIP.equalsIgnoreCase("")) {
			allowPattern = Pattern.compile(allowIP);
		} else {
			allowPattern = null; //for unit test
		}
		if(forbidIP != null && !forbidIP.equalsIgnoreCase("")) {
			forbidPattern = Pattern.compile(forbidIP);
		} else {
			forbidPattern = null; //for unit test
		}
	}
	
	/**
	 * check ip is allow
	 * @param ip
	 * @return
	 */
	public static boolean isAllow(String ip) {
		if(ip != null && !ip.equalsIgnoreCase("")) {
			boolean allowMatch = true;
			boolean forbidMatch = false;
			
			if(allowPattern != null) {
				allowMatch = allowPattern.matcher(ip).find();
			}
			if(forbidPattern != null) {
				forbidMatch = forbidPattern.matcher(ip).find();
			}
			
			return (allowMatch && !forbidMatch); 
		}

		return false;
	}
	
	/**
	 * format ip
	 * @param ip
	 * @return
	 */
	public static String formatIP(String ip) {
		ip = ip.replaceAll("/", "");
		ip = ip.substring(0, ip.lastIndexOf(":"));
		return ip;
	}
}