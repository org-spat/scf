package org.spat.scf.server.performance.monitor;

import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;

import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.performance.JVMMonitor;
import org.spat.scf.server.performance.MonitorGC;
import org.spat.scf.server.performance.MonitorMemory;

/**
 * MonitorJVM JVM
 * @author Service Platform Architecture Team 
 */
public class MonitorJVM {
	
	private static ILog logger = LogFactory.getLogger(MonitorJVM.class);
	private MonitorUDPClient udp;
	private String serviceName;
	
	public MonitorJVM(MonitorUDPClient udp, String serviceName) {
		this.udp = udp;
		this.serviceName = serviceName;
	}
	
	/**
	 * 获取内存使用情况
	 */	
	public void jvmGc() {
		if(udp == null) {
			return;
		}
		MonitorMemory monitorMemory = JVMMonitor.getMemoryUsed();
		StringBuffer strb = new StringBuffer();
		strb.append(monitorMemory.getSurvivor().getCommitted());
		strb.append("\t");
		strb.append(monitorMemory.getSurvivor().getUsed());
		strb.append("\t");
		strb.append(monitorMemory.getPerm().getCommitted());
		strb.append("\t");
		strb.append(monitorMemory.getPerm().getUsed());
		strb.append("\t");
		strb.append(monitorMemory.getEden().getCommitted());
		strb.append("\t");
		strb.append(monitorMemory.getEden().getUsed());
		strb.append("\t");
		strb.append(monitorMemory.getOld().getCommitted());
		strb.append("\t");
		strb.append(monitorMemory.getOld().getUsed());
		strb.append("\t");
		strb.append(serviceName);
		byte[] responseByte;
		
		try {
			responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.Gc);
			udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send jvmGc error");
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取堆内存gc数据
	 */
	public void jvmGCUtil() {
		MonitorMemory monitorMemory = JVMMonitor.getMemoryUsed();
		MonitorGC monitorGC=JVMMonitor.getGcTime();
		DecimalFormat df = new DecimalFormat( "0.00"); 
		StringBuffer strb = new StringBuffer();
		strb.append(df.format(monitorMemory.getSurvivor().getPercentage()));
		strb.append("\t");
		strb.append(df.format(monitorMemory.getEden().getPercentage()));
		strb.append("\t");
		strb.append(df.format(monitorMemory.getOld().getPercentage()));
		strb.append("\t");
		strb.append(df.format(monitorMemory.getPerm().getPercentage()));
		strb.append("\t");
		strb.append(df.format(monitorMemory.getCodeCache().getPercentage()));
		strb.append("\t");
		strb.append(monitorGC.getyGcCount());
		strb.append("\t");
		strb.append(monitorGC.getyGcTime());
		strb.append("\t");
		strb.append(monitorGC.getfGcCount());
		strb.append("\t");
		strb.append(monitorGC.getfGcTime());
		strb.append("\t");
		strb.append(monitorGC.getGcTime());
		strb.append("\t");
		strb.append(serviceName);
		byte[] responseByte;
		
		try {
			responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.GCUtil);
			udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send jvmGCUtil error");
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取线程数
	 */
	public void jvmThreadCount() {
		StringBuilder strb = new StringBuilder();
		strb.append(JVMMonitor.getAllThreadsCount());
		strb.append("\t");
		strb.append(JVMMonitor.getPeakThreadCount());
		strb.append("\t");
		strb.append(JVMMonitor.getDaemonThreadCount());
		strb.append("\t");
		strb.append(JVMMonitor.getTotalStartedThreadCount());
		strb.append("\t");
		strb.append(JVMMonitor.getDeadLockCount());
		strb.append("\t");
		strb.append(serviceName);
		
		byte[] responseByte;
		try {
			responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.ThreadCount);
			udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send jvmThreadCount error");
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取JVM的class数
	 */
	public void jvmClassCount(){
		StringBuffer strb = new StringBuffer();
		strb.append(JVMMonitor.getLoadedClassCount());
		strb.append("\t");
		strb.append(JVMMonitor.getUnloadedClassCount());
		strb.append("\t");
		strb.append(JVMMonitor.getTotalLoadedClassCount());	
		strb.append("\t");
		strb.append(serviceName);
		byte[] responseByte;
		try {
			responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.ClassCount);
			udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send jvmClassCount error");
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取JVM内存使用情况
	 */
	public void jvmMemory() {
		StringBuilder strb = new StringBuilder();
		strb.append(JVMMonitor.getTotolMemory());
		strb.append("\t");
		strb.append(JVMMonitor.getUsedMemory());
		strb.append("\t");
		strb.append(JVMMonitor.getMaxUsedMemory());
		strb.append("\t");
		strb.append(serviceName);
		byte[] responseByte;
		try {
			responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.Memory);
			udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send JVMMemory error");
			e.printStackTrace();
		}
	}
	
	/**
	 *  获取虚拟机的heap内存使用情况
	 */
	public void jvmHeapMemory() {
		MemoryUsage memoryUsage=JVMMonitor.getJvmHeapMemory();
		StringBuilder strb = new StringBuilder();
		strb.append(memoryUsage.getInit());
		strb.append("\t");
		strb.append(memoryUsage.getCommitted());
		strb.append("\t");
		strb.append(memoryUsage.getMax());
		strb.append("\t");
		strb.append(memoryUsage.getUsed());
		strb.append("\t");
		strb.append(serviceName);
		byte[] responseByte;
		try {
			responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.HeapMemory);
			udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send JVMMemory error");
			e.printStackTrace();
		}
	}
	
	/**
	 *  获取虚拟机的noheap内存使用情况
	 */
	public void jvmNoHeapMemory() {
		MemoryUsage memoryUsage=JVMMonitor.getJvmNoHeapMemory();
		StringBuilder strb = new StringBuilder();
		strb.append(memoryUsage.getInit());
		strb.append("\t");
		strb.append(memoryUsage.getCommitted());
		strb.append("\t");
		strb.append(memoryUsage.getMax());
		strb.append("\t");
		strb.append(memoryUsage.getUsed());
		strb.append("\t");
		strb.append(serviceName);
		byte[] responseByte;
		try {
			responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.NoHeapMemory);
			udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send JVMMemory error");
			e.printStackTrace();
		}
	}
	
	/**
	 * 系统负载
	 */
	public void jvmLoad() {
		StringBuilder strb = new StringBuilder();
		strb.append(JVMMonitor.getSystemLoad());
		strb.append("\t");
		strb.append(serviceName);
		byte[] responseByte;
		try {
			responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.Load);
			udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send JVMMemory error");
			e.printStackTrace();
		}
	}
}
