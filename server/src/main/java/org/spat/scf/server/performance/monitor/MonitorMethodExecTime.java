package org.spat.scf.server.performance.monitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.spat.scf.server.contract.context.StopWatch;
import org.spat.scf.server.contract.context.StopWatch.PerformanceCounter;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;

public class MonitorMethodExecTime {

	final static ILog logger = LogFactory.getLogger(MonitorMethodExecTime.class);
	private static Map<String, MethodExeInfo> methodExeTime = new ConcurrentHashMap<String, MethodExeInfo>();
	private static Map<String, Integer> methodTimeResult = new HashMap<String, Integer>();
	
	public static void messageRecv(final StopWatch sw) {
		Collection<PerformanceCounter> pcList = sw.getMapCounter().values();
		for(PerformanceCounter pc : pcList) {
			String key = pc.getKey();
			synchronized (methodExeTime) {
			if (methodExeTime.containsKey(key)) {
//				logger.error("methodname" + key + "exetime:" + pc.getExecuteTime() + "times" + methodExeTime.get(key).getCount());
				methodExeTime.get(key).addTotalExeTime(pc.getExecuteTime());
				methodExeTime.get(key).countGetAndIncrement();
			} else {
				MethodExeInfo me = new MethodExeInfo(key, pc.getExecuteTime());
				methodExeTime.put(key, me);
			}
		}
	}
	}
	
	public Map<String, MethodExeInfo> getMethodExeTime() {
		return methodExeTime;
	}
	
	public Map<String, Integer> getmethodAvgTime() {
		return methodTimeResult;
	}
	
	public Map<String, Integer> computeAveTime() {
		try {
//			将methodExeTime中的内容转移到methodExeTimeList中进行操作
			Map<String, MethodExeInfo> timeList = null;
			synchronized (methodExeTime) {

				timeList = MoveElement(methodExeTime);
				}
			
			computAverage(timeList, methodTimeResult);
			
		} catch(Exception e) {
			logger.error(e);
		}
		return methodTimeResult;
	}
	
	public Map<String, MethodExeInfo> MoveElement(Map<String, MethodExeInfo> methodExeTime) {
		Map<String, MethodExeInfo> execTimeList = new HashMap<String, MethodExeInfo>();
		if (!methodExeTime.isEmpty()) {
			for(Map.Entry<String, MethodExeInfo> m : methodExeTime.entrySet()) {
				execTimeList.put(m.getKey(), m.getValue());
			}
		}
		methodExeTime.clear();
		return execTimeList;
	}
	public static void computAverage(Map<String, MethodExeInfo> timeList, Map<String, Integer> methodTimeResult) {
		methodTimeResult.clear();
		for(Map.Entry<String, MethodExeInfo> m : timeList.entrySet()) {
			Integer avgTime = (int) (m.getValue().getTotalExeTime()/m.getValue().getCount());
			logger.debug("exeTime " + m.getValue().getTotalExeTime());
			logger.debug("exeTimes " + m.getValue().getCount());
			methodTimeResult.put(m.getKey(), avgTime);
		}
	}
}
class MethodExeInfo {
	private String methodName;
	private long totalExeTime;
	private AtomicInteger count= new AtomicInteger(0);
	
	public MethodExeInfo () {
		
	}

	public MethodExeInfo(String methodName, long totalExeTime) {
		this.methodName = methodName;
		this.totalExeTime = totalExeTime;
	}
	
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public long getTotalExeTime() {
		return totalExeTime;
	}

	public void setTotalExeTime(long totalExeTime) {
		this.totalExeTime = totalExeTime;
	}
	
	public void addTotalExeTime(long exeTime) {
		this.totalExeTime += exeTime;
	}

	public int getCount() {
		return count.get();
	}

	public int countGetAndIncrement() {
		return count.getAndIncrement();
	}
	
}
