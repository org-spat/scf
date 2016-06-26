package org.spat.scf.server.performance;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * JVM 监控
 */
public class JVMMonitor {
    private static final OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static final long maxMemory = Runtime.getRuntime().maxMemory();
    private static final ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
    private static final List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    private static final List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
    private static final Set<String> edenSpace = new HashSet<String>();
    private static final Set<String> survivorSpace = new HashSet<String>();
    private static final Set<String> oldSpace = new HashSet<String>();
    private static final Set<String> permSpace = new HashSet<String>();
    private static final Set<String> codeCacheSpace = new HashSet<String>();
    private static final List<String> youngGenCollectorNames = new ArrayList<String>();
    private static final List<String> fullGenCollectorNames = new ArrayList<String>();
    static {
        // 各种GC下的eden名字
        edenSpace.add("Eden Space");// -XX:+UseSerialGC
        edenSpace.add("PS Eden Space");// –XX:+UseParallelGC
        edenSpace.add("Par Eden Space");// -XX:+UseConcMarkSweepGC
        edenSpace.add("Par Eden Space");// -XX:+UseParNewGC
        edenSpace.add("PS Eden Space");// -XX:+UseParallelOldGC
        // 各种gc下survivorSpace的名字
        survivorSpace.add("Survivor Space");// -XX:+UseSerialGC
        survivorSpace.add("PS Survivor Space");// –XX:+UseParallelGC
        survivorSpace.add("Par Survivor Space");// -XX:+UseConcMarkSweepGC
        survivorSpace.add("Par survivor Space");// -XX:+UseParNewGC
        survivorSpace.add("PS Survivor Space");// -XX:+UseParallelOldGC
        // 各种gc下oldspace的名字
        oldSpace.add("Tenured Gen");// -XX:+UseSerialGC
        oldSpace.add("PS Old Gen");// –XX:+UseParallelGC
        oldSpace.add("CMS Old Gen");// -XX:+UseConcMarkSweepGC
        oldSpace.add("Tenured Gen  Gen");// Tenured Gen Gen
        oldSpace.add("PS Old Gen");// -XX:+UseParallelOldGC

        // 各种gc下持久代的名字
        permSpace.add("Perm Gen");// -XX:+UseSerialGC
        permSpace.add("PS Perm Gen");// –XX:+UseParallelGC
        permSpace.add("CMS Perm Gen");// -XX:+UseConcMarkSweepGC
        permSpace.add("Perm Gen");// -XX:+UseParNewGC
        permSpace.add("PS Perm Gen");// -XX:+UseParallelOldGC
        // codeCache的名字
        codeCacheSpace.add("Code Cache");
        // Oracle (Sun) HotSpot
        // -XX:+UseSerialGC
        youngGenCollectorNames.add("Copy");
        // -XX:+UseParNewGC
        youngGenCollectorNames.add("ParNew");
        // -XX:+UseParallelGC
        youngGenCollectorNames.add("PS Scavenge");
        // Oracle (Sun) HotSpot
        // -XX:+UseSerialGC
        fullGenCollectorNames.add("MarkSweepCompact");
        // -XX:+UseParallelGC and (-XX:+UseParallelOldGC or -XX:+UseParallelOldGCCompacting)
        fullGenCollectorNames.add("PS MarkSweep");
        // -XX:+UseConcMarkSweepGC
        fullGenCollectorNames.add("ConcurrentMarkSweep");

    }
    /**
     * 获取进程pid
     * 
     * @return
     * */
    public static String getProcessorsId(){
    	return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    }

    /**
     * @return MBeanServer
     */
    static MBeanServer getPlatformMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }

    /**
     * 获取系统负载
     * 
     * @return
     */
    public static double getSystemLoad() {
        if (!(bean instanceof OperatingSystemMXBean)){
            return 0L;
        }
        double sl = ((OperatingSystemMXBean) bean).getSystemLoadAverage();
        if(sl < 0){
        	return 0L;
        }
        return sl;
    }

    /**
     * 获取CPU个数
     * 
     * @return
     */
    public static int getAvailableProcessors() {
        if (!(bean instanceof OperatingSystemMXBean))
            return 0;
        return ((OperatingSystemMXBean) bean).getAvailableProcessors();
    }

    /**
     * 返回文件描述符数
     * 
     * @return
     */
    public static String getFileDescriptor() {
        try {
            String[] attributeNames = new String[] { "MaxFileDescriptorCount", "OpenFileDescriptorCount" };
            ObjectName name;
            name = new ObjectName("java.lang:type=OperatingSystem");
            AttributeList attributes = getPlatformMBeanServer().getAttributes(name, attributeNames);
            StringBuilder sb = new StringBuilder();
            for (int i = 0, len = attributes.size(); i < len; i++) {
                if (sb.length() > 0) {
                    sb.append("\r\n");
                }
                sb.append(attributes.get(i).toString().replace(" = ", ":"));
            }
            return sb.toString();
        } catch (MalformedObjectNameException e) {
            return "MaxFileDescriptorCount:0\r\nOpenFileDescriptorCount:0";
        } catch (NullPointerException e) {
            return "MaxFileDescriptorCount:0\r\nOpenFileDescriptorCount:0";
        } catch (InstanceNotFoundException e) {
            return "MaxFileDescriptorCount:0\r\nOpenFileDescriptorCount:0";
        } catch (ReflectionException e) {
            return "MaxFileDescriptorCount:0\r\nOpenFileDescriptorCount:0";
        }
    }

    /**
     * 获取所有的线程数
     * 
     * @return
     */
    public static int getAllThreadsCount() {
        return threadBean.getThreadCount();
    }

    /**
     * 获取峰值线程数
     * 
     * @return
     */
    public static int getPeakThreadCount() {
        return threadBean.getPeakThreadCount();
    }

    /**
     * 获取守护线程数
     * 
     * @return the current number of live daemon threads.
     */
    public static int getDaemonThreadCount() {
        return threadBean.getDaemonThreadCount();
    }

    /**
     * 获取启动以来创建的线程数
     * 
     * @return
     */
    public static long getTotalStartedThreadCount() {
        return threadBean.getTotalStartedThreadCount();
    }

    /**
     * 获取死锁数
     * 
     * @return 死锁数
     */
    public static int getDeadLockCount() {
        ThreadMXBean th = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        long[] deadLockIds = th.findMonitorDeadlockedThreads();
        if (deadLockIds == null) {
            return 0;
        } else {
            return deadLockIds.length;
        }

    }

    /**
     * 获取虚拟机的heap内存使用情况
     * 
     * @return
     */
    public static MemoryUsage getJvmHeapMemory() {
        return memoryMXBean.getHeapMemoryUsage();

    }

    /**
     * 获取虚拟机的noheap内存使用情况
     * 
     * @return
     */
    public static MemoryUsage getJvmNoHeapMemory() {
        return memoryMXBean.getNonHeapMemoryUsage();

    }

    /**
     * 获取当前JVM占用的总内存
     * 
     * @return
     */
    public static long getTotolMemory() {
        long totalMemory = Runtime.getRuntime().totalMemory();

        return totalMemory;
    }

    /**
     * 获取当前JVM给应用分配的内存
     * 
     * @return
     */
    public static long getUsedMemory() {
        long totalMemory = Runtime.getRuntime().totalMemory();
        long usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
        return usedMemory;
    }

    /**
     * 获取JVM能使用到的最大内存
     * 
     * @return
     */
    public static long getMaxUsedMemory() {
        return maxMemory;
    }

    /**
     * 获取启动以来加载的总的class数
     * 
     * @return
     */
    public static long getTotalLoadedClassCount() {
        return classLoadingBean.getTotalLoadedClassCount();
    }

    /**
     * 获取当前JVM加载的class数
     * 
     * @return
     */
    public static int getLoadedClassCount() {
        return classLoadingBean.getLoadedClassCount();
    }

    /**
     * 获取JVM被启动以来unload的class数
     * 
     * @return
     */
    public static long getUnloadedClassCount() {

        return classLoadingBean.getUnloadedClassCount();
    }

    /**
     * 获取GC的时间
     * 
     * @return
     */
    public static MonitorGC getGcTime(){
    	MonitorGC monitorGC=new MonitorGC();
        for (GarbageCollectorMXBean bean : garbageCollectorMXBeans) {
            if (youngGenCollectorNames.contains(bean.getName())) {
            	monitorGC.setyGcCount(bean.getCollectionCount());
            	monitorGC.setyGcTime(bean.getCollectionTime());
            } else if (fullGenCollectorNames.contains(bean.getName())) {
            	monitorGC.setfGcCount(bean.getCollectionCount());
            	monitorGC.setfGcTime(bean.getCollectionTime());
            }

        }
        monitorGC.setGcTime(monitorGC.getfGcTime()+monitorGC.getyGcTime());
    	return monitorGC;
    }
    

    public static Map<String, MemoryUsage> getMemoryPoolCollectionUsage() {
        Map<String, MemoryUsage> gcMemory = new HashMap<String, MemoryUsage>();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            String name = memoryPoolMXBean.getName();
            if (edenSpace.contains(name)) {
                gcMemory.put("eden", memoryPoolMXBean.getCollectionUsage());
            } else if (survivorSpace.contains(name)) {
                gcMemory.put("survivor", memoryPoolMXBean.getCollectionUsage());
            } else if (oldSpace.contains(name)) {
                gcMemory.put("old", memoryPoolMXBean.getCollectionUsage());
            } else if (permSpace.contains(name)) {
                gcMemory.put("perm", memoryPoolMXBean.getCollectionUsage());
            } else if (codeCacheSpace.contains(name)) {
                gcMemory.put("codeCache", memoryPoolMXBean.getCollectionUsage());
            }

        }
        return gcMemory;
    }
    
  

    public static Map<String, MemoryUsage> getMemoryPoolUsage() {
        Map<String, MemoryUsage> gcMemory = new HashMap<String, MemoryUsage>();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            String name = memoryPoolMXBean.getName();
            if (edenSpace.contains(name)) {
                gcMemory.put("eden", memoryPoolMXBean.getUsage());
            } else if (survivorSpace.contains(name)) {
                gcMemory.put("survivor", memoryPoolMXBean.getUsage());
            } else if (oldSpace.contains(name)) {
                gcMemory.put("old", memoryPoolMXBean.getUsage());
            } else if (permSpace.contains(name)) {
                gcMemory.put("perm", memoryPoolMXBean.getUsage());
            } else if (codeCacheSpace.contains(name)) {
                gcMemory.put("codeCache", memoryPoolMXBean.getUsage());
            }

        }
        return gcMemory;
    }
    
   /**
    * 获取堆内存使用情况
    * 
    * @return
    * */
    public static MonitorMemory getMemoryUsed(){
    	MonitorMemory monitorMemory=new MonitorMemory();
    	
    	for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
        	String name = memoryPoolMXBean.getName();
            if(edenSpace.contains(name)) {
            	monitorMemory.setEden(mUsageToMStat(memoryPoolMXBean));
            }else if(survivorSpace.contains(name)) {
            	monitorMemory.setSurvivor(mUsageToMStat(memoryPoolMXBean));
            }else if(oldSpace.contains(name)) {
            	monitorMemory.setOld(mUsageToMStat(memoryPoolMXBean));
            }else if(permSpace.contains(name)) {
            	monitorMemory.setPerm(mUsageToMStat(memoryPoolMXBean));
            }else if(codeCacheSpace.contains(name)) {
            	 monitorMemory.setCodeCache(mUsageToMStat(memoryPoolMXBean));
            }
         }
    	return monitorMemory;
    }
    
    public static MemoryStat mUsageToMStat(MemoryPoolMXBean memoryPoolMXBean){
    	 
    	MemoryStat memoryStat=new MemoryStat();
    	memoryStat.setCommitted(memoryPoolMXBean.getUsage().getCommitted());
    	memoryStat.setInit(memoryPoolMXBean.getUsage().getInit());
    	memoryStat.setMax(memoryPoolMXBean.getUsage().getMax());
    	memoryStat.setUsed(memoryPoolMXBean.getUsage().getUsed());
    	memoryStat.setPercentage((double)memoryPoolMXBean.getUsage().getUsed()/(double)memoryPoolMXBean.getUsage().getInit()*100);
    	return memoryStat;
    }
    
}
