package org.spat.utility.async;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.spat.utility.jsr166.LinkedTransferQueue;
import org.spat.utility.jsr166.TransferQueue;
import org.spat.utility.tools.InetAddressUtil;


class AsyncWorker extends Thread {
	
	private static final Log logger = LogFactory.getLog(AsyncWorker.class);
	final String threadFactoryName;
	static String localIp = InetAddressUtil.getIpMixed();
	
	/**
	 * 共享的任务队列
	 */
	private final TransferQueue<AsyncTask> taskQueue;
	
	/**
	 * 任务执行器
	 */
	private final Executor executor;
	
	/**
	 * 是否结束
	 */
	private boolean isStop = false;
	
	private boolean timeoutEffect = false;

	
	AsyncWorker(Executor executor, boolean timeoutEffect, String threadFactoryName) {
		this.taskQueue = new LinkedTransferQueue<AsyncTask>();
		this.executor = executor;
		this.timeoutEffect = timeoutEffect;
		this.threadFactoryName = threadFactoryName;
	}
	
	@Override
	public void run() {
		if(this.timeoutEffect) {
			while(!isStop) {
				execTimeoutTask();
			}
		} else {
			while(!isStop) {
				execNoTimeLimitTask();
			}
		}
	}
	
	/**
	 * 添加任务
	 * @param task
	 */
	void addTask(AsyncTask task) {
		this.taskQueue.offer(task);
	}

	/**
	 * 停止工作线程(stop is final)
	 */
	void end() {
		this.isStop = true;
		logger.info("-------------------async workder is stop-------------------");
	}
	
	private void execNoTimeLimitTask() {
		AsyncTask task = null;
		try {
			task = taskQueue.poll(1500, TimeUnit.MILLISECONDS);
			if(null != task){
				if((System.currentTimeMillis() - task.getAddTime()) > task.getQtimeout()) {
					task.getHandler().exceptionCaught(new TimeoutException(threadFactoryName+" async task timeout!" + " Host ip:" + localIp));
					return;
				} else {
					if(task.getInQueueTime() != -1 && (System.currentTimeMillis() - task.getAddTime()) >= task.getInQueueTime()) {
						logger.error(threadFactoryName + " The task inQueue time :" + (System.currentTimeMillis() - task.getAddTime()));
					}
					Object obj = task.getHandler().run();
					task.getHandler().messageReceived(obj);
				}
			}
		} catch(InterruptedException ie) {
			
		} catch(Throwable ex) {
			if(task != null) {
				task.getHandler().exceptionCaught(ex);
			}
		}
	}
	
	private void execTimeoutTask() {
		try {
			final AsyncTask task = taskQueue.poll(1500, TimeUnit.MILLISECONDS);
			if(null != task) {
				if((System.currentTimeMillis() - task.getAddTime()) > task.getQtimeout()) {
					task.getHandler().exceptionCaught(new TimeoutException(threadFactoryName+"async task timeout!" + " Host ip:" + localIp));
					return;
				}else{
					final CountDownLatch cdl = new CountDownLatch(1);
					executor.execute(new Runnable(){
							@Override
							public void run() {
								try {
									Object obj = task.getHandler().run();
									task.getHandler().messageReceived(obj);
								} catch(Throwable ex) {
									task.getHandler().exceptionCaught(ex);
								} finally {
									cdl.countDown();
								}
							}
						}
					);
					cdl.await(getTimeout(task.getTimeout(), taskQueue.size()), TimeUnit.MILLISECONDS);
					if(cdl.getCount() > 0) {
						task.getHandler().exceptionCaught(new TimeoutException(threadFactoryName+"async task timeout!" + " Host ip:" + localIp));
					}
				}
			}
		} catch(InterruptedException ie) {
			logger.error("");
		} catch (Throwable e) {
			logger.error("get task from poll error", e);
		}
	}
	
	/**
	 * 获得超时时间(队列越长，超时时间越短，最长不会超过初始给定的值)
	 * @param timeout
	 * @param queueLen
	 * @return
	 */
	private int getTimeout(int timeout, int queueLen) {
        if (queueLen <= 0 || timeout < 5) {
            return timeout;
        }
        
        float rad = (float) ((float)timeout - (float)timeout * 0.006 * queueLen);
        int result = (int)rad < 5 ? 5 : (int)rad;
        if (queueLen > 100) {
            logger.warn("async task,queueLen:" + queueLen + ",fact timeout:" + result + ",original timeout:" + timeout);
        }
        
        return result;
    }
}