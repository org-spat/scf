package org.spat.utility.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ConfigMonitor {
	
	public static List<IConfig> configList = new ArrayList<IConfig>();
	
	private Timer configTimer = new Timer("config");
	
	public ConfigMonitor() {
		configTimer.schedule(monitor, 1 * 1000, 1 * 1000);
	}
	
	TimerTask monitor = new TimerTask() {
		public void run() {
			for(IConfig config : configList) {
				config.onChange();
			}
		}
	};
}