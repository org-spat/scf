package org.spat.scf.server.performance.monitor;

import java.util.concurrent.atomic.AtomicInteger;

import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;

/**
 * AbandonCount 任务超时计数
 * @author Service Platform Architecture Team 
 */
public class AbandonCount {
	static ILog logger = LogFactory.getLogger(AbandonCount.class);
	private static AtomicInteger count = new AtomicInteger(0);
	
	public static void messageRecv() {
		count.getAndIncrement();
	}
	
	public static int getCount() {
		return count.get();
	}
	
	public static void initCount(int i) {
		count.set(i);
	}
}
