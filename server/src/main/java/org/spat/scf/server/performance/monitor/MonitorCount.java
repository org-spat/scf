package org.spat.scf.server.performance.monitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.spat.scf.server.contract.context.StopWatch;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;

public class MonitorCount {

	final static ILog logger = LogFactory.getLogger(MonitorCount.class);
	private static AtomicInteger count = new AtomicInteger(0);
	private static Map<String, Integer> fromIP = new ConcurrentHashMap<String, Integer>();

	public static void messageRecv(final StopWatch sw) {
		if (sw == null) {
			return;
		}

		count.getAndIncrement();
		String ip = sw.getFromIP();
		int countIP = 0;
		synchronized (fromIP) {
		if (MonitorCount.fromIP.containsKey(ip)) {
			countIP = MonitorCount.fromIP.get(ip) + 1;
			MonitorCount.fromIP.put(ip, countIP);
		} else {
			MonitorCount.fromIP.put(ip, 1);
			}
		}
	}

	public int getCount() {
		return count.get();
	}

	public static void initCount(int i) {
		count.set(i);
	}

	/**
	 * 统计前初始化
	 * 
	 * */
	public void initMCount() {
		if (fromIP != null) {
			fromIP.clear();
		}
		initCount(0);
	}

	public static Map<String, Integer> getFromIP() {
		return fromIP;
	}

}
