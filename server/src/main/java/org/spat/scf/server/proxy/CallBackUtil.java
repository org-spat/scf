package org.spat.scf.server.proxy;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.spat.scf.server.utility.SystemUtils;
import org.spat.scf.server.contract.context.Global;

public class CallBackUtil {

	private static final Log logger = LogFactory.getLog(CallBackUtil.class);
	private static final int COUNT = SystemUtils.getHalfCpuProcessorCount();
	private final LinkedBlockingQueue<WData> checkQueue = new LinkedBlockingQueue<WData>();
	
	long taskTimeOut = 1000L;//将int类型修改为int类型
	private Thread[] workers;
	private static long time = System.currentTimeMillis();
	
	public CallBackUtil(){
		String sTaskTimeOut = Global.getSingleton().getServiceConfig().getString("back.task.timeout");
		if(sTaskTimeOut != null && !"".equals(sTaskTimeOut)){
			taskTimeOut = Long.parseLong(sTaskTimeOut);
			taskTimeOut = ((taskTimeOut * 3)/2) + 1;
		}
		
		workers = new Thread[COUNT];
		for (int i = 0; i < COUNT; i++) {
			workers[i] = new Thread(new CallBackHandle());
			workers[i].setName("CallBackHandle thread[" + i + "]");
			workers[i].setDaemon(true);
			workers[i].start();
		}
	}
	
	public void offer(WData wd){
		checkQueue.offer(wd);
	}
	
	class CallBackHandle implements Runnable {
		
		@Override
		public void run() {
			for(;;){
				try {
					WData wd = checkQueue.poll(1500, TimeUnit.MILLISECONDS);
					if(wd != null){
						
						if ((System.currentTimeMillis() - wd.getTime()) < taskTimeOut) {
							Thread.sleep(taskTimeOut - (System.currentTimeMillis() - wd.getTime()));
						}
						
						if(AsynBack.contextMap.get(wd.getSessionID()).isDel()){
							AsynBack.contextMap.remove(wd.getSessionID());//使用完删除context
							AsynBack.swInvokeKeyMap.remove(wd.getSessionID());
							continue;
						} else {
							AsynBack.send(wd.getSessionID(), new Exception("wait other server recive timeout.wait time is "+taskTimeOut));
							AsynBack.contextMap.remove(wd.getSessionID());//使用完删除context
							AsynBack.swInvokeKeyMap.remove(wd.getSessionID());
						}

					}
				} catch (InterruptedException e) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}catch (Exception ex) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
					ex.printStackTrace();
				}
			}
		}
	}
}
