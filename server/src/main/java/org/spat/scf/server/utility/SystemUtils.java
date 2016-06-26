package org.spat.scf.server.utility;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public final class SystemUtils {

	private static final ReentrantLock lock = new ReentrantLock();
	private static final AtomicInteger sessionID = new AtomicInteger(0);
	private static final long MAX_SESSIONID = 1024 * 1024 * 1024;

	private SystemUtils() {

	}

	/**
	 * 默认为CPU个数-1，留一个CPU做网卡中断
	 * 
	 * @return
	 */
	public static int getSystemThreadCount() {
		final int cpus = getCpuProcessorCount();
		final int result = cpus - 1;
		return result == 0 ? 1 : result;
	}

	public static int getCpuProcessorCount() {
		return Runtime.getRuntime().availableProcessors();
	}

	public static int getHalfCpuProcessorCount() {
		final int cpu = getCpuProcessorCount();
		int n = cpu / 2;
		if (cpu < 7) {
			n = cpu;
		}
		return (n > 6) ? 6 : n;
	}

	public static int createSessionId() {
		try {
			lock.lock();
			int sID = sessionID.getAndIncrement();
			if (sessionID.getAndIncrement() > SystemUtils.MAX_SESSIONID) {
				sessionID.set(1);
			}
			return sID;
		} finally {
			lock.unlock();
		}
	}
}
