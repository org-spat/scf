package org.spat.scf.client.utility;

public final class SystemUtils {
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
    
    public static int getHalfCpuProcessorCount(){
    	final int cpu = getCpuProcessorCount();
    	int n = cpu / 2;
    	if(cpu < 7){
    		n = cpu;
    	}
    	return (n > 6)? 6 : n;
    }
}
