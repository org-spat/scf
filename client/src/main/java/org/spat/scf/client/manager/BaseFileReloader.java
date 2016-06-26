package org.spat.scf.client.manager;

import java.io.File;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class BaseFileReloader {
	
	private boolean boot_read = false; // 是否已经启动加载
	
	private long lastUpdateTime = 0; // 最后一次修改时间

	private String filePath; // 文件路径
	
	private long initialDelay; // 延时执行时间(s)
	
	private long delay; // 重复执行间隔时间(s)
	
	private boolean started = false; // 是否已经启动
	
	protected static ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);

	protected abstract boolean reload() throws Exception;
	
	protected BaseFileReloader(String filePath, long initialDelay, long delay) {
		this.filePath = filePath;
		this.initialDelay = initialDelay;
		this.delay = delay;
		this.startMonitor();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected final File getFile(){
		File file = null;
		try{
			file = new File(filePath);
		}catch (Exception e) {
			e.printStackTrace();
		}
//		if(file==null || !file.exists())
//			System.err.println(filePath + " not exist !");
		return file;
	}
	
	private final boolean needReload(){
		File file = getFile();
			
		if(file!=null && (!boot_read || lastUpdateTime!=file.lastModified()))
			return true;
		
		return false;
	}
	
	private long getFileLastModifiedTime(){
		File file = getFile();
		if(file==null)
			return 0;
		return file.lastModified();
	}
	
	private final TimerTask timerTask = new TimerTask(){
		@Override
		public void run() {
			if(needReload()){
				try{
					reload();
					boot_read = true;
					lastUpdateTime=getFileLastModifiedTime();
					System.out.println(filePath + " reloaded !");
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	protected final void startMonitor() {
		if(started)
			return;
		scheduledExecutor.scheduleWithFixedDelay(timerTask, initialDelay, delay, TimeUnit.SECONDS);
		started = true;
	}
	
	protected final boolean isStarted() {
		return started;
	}

}
