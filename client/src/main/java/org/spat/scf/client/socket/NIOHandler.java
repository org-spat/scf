package org.spat.scf.client.socket;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;

public class NIOHandler {
	private static final Log logger = LogFactory.getLog(NIOHandler.class);
	private static final NIOHandler handler = new NIOHandler();
	final int q_size = 30000;
	private final static ThreadPoolExecutor writeExe = new ThreadPoolExecutor(1, 
			1, 1500L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadRenameFactory("NIOHandler-Send-Thread"));
	private final static ThreadPoolExecutor timeOutExe = new ThreadPoolExecutor(4, 
			8, 1500L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadRenameFactory("NIOHandler-TimeOut-Thread"));
	
	private NIOHandler() {
		
	}
	
	public static NIOHandler getInstance() {
		return handler;
	}

	public void offerWriteData(final WindowData wd) {
		if(getWriteQueueSize() > q_size || getTimeOutQueueSize() > q_size){
			logger.warn("writeQueue size > "+q_size);
			wd.getReceiveHandler().callBack(new Exception("writeQueue size > "+q_size));
			return;
		}
		try {
			sendInvoke(wd);
			timeOutInvoke(wd);
		} catch (Exception e) {
			logger.warn("input queue error");
			wd.getReceiveHandler().callBack(new Exception("input queue error"));
			return;
		}
	}
	
	private void sendInvoke(final WindowData wd){
		writeExe.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if(wd != null) {						
						switch (wd.getFlag()) {
						case 1:
							wd.getCsocket().send(wd.getSendData());
							break;
						default:
							break;
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void timeOutInvoke(final WindowData wd){
		timeOutExe.execute(new Runnable() {
			@Override
			public void run() {
				try{
					if(wd != null) {
						if(System.currentTimeMillis() - wd.getTimestamp() > wd.getCsocket().getTimeOut(getWriteQueueSize())) {
							String exceptionMsg = "ServiceName:[" + wd.getCsocket().getServiceName() + "],ServiceIP:[" + wd.getCsocket().getServiceIP() + "],Receive data timeout or error!timeout:" + (System.currentTimeMillis() - wd.getTimestamp());
							wd.getReceiveHandler().callBack(new Exception(exceptionMsg));
							wd.getCsocket().unregisterRec(wd.getSessionId());
						}else {
							if(wd.getCsocket().hasSessionId(wd.getSessionId())) {
								timeOutInvoke(wd);
								Thread.sleep(1);
							}
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}
	
	public static int getWriteQueueSize() {
		return writeExe.getQueue().size();
	}
	
	public static int getTimeOutQueueSize() {
		return timeOutExe.getQueue().size();
	}
}

