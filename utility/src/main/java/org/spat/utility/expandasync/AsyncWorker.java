package org.spat.utility.expandasync;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AsyncWorker {

	private static final Log logger = LogFactory.getLog(AsyncWorker.class);
	final String threadFactoryName;
	private ExecutorService exec = null;
	
	/**
	 * 共享的任务队列
	 */
	private final BlockingQueue<AsyncTask> taskQueue;
	
	/**
	 * 是否结束
	 */
	private boolean isStop = false;
	
	private int subWokerCount = 1;
	
	AsyncWorker(String threadFactoryName) {
		this.taskQueue = new LinkedBlockingQueue<AsyncTask>();
		this.threadFactoryName = threadFactoryName;
	}
	
	AsyncWorker(int limitSize, String threadFactoryName) {
		if (limitSize == 0) {
			this.taskQueue = new LinkedBlockingQueue<AsyncTask>();
		} else {
			this.taskQueue = new LinkedBlockingQueue<AsyncTask>(limitSize);
		}
		this.threadFactoryName = threadFactoryName;
	}
	
	AsyncWorker(int subWorkerCount, int limitSize, String threadFactoryName) {
		if (limitSize == 0) {
			this.taskQueue = new LinkedBlockingQueue<AsyncTask>();
		} else {
			this.taskQueue = new LinkedBlockingQueue<AsyncTask>(limitSize);
		}
		if (subWorkerCount <= 0) {
			this.subWokerCount = 1;
		} else {
			this.subWokerCount = subWorkerCount;
		}
		this.threadFactoryName = threadFactoryName;
	}
	
	/**
	 * 添加任务  
	 * @param task
	 */
	void addTask(AsyncTask task) {
		this.taskQueue.offer(task);
	}

	/**
	 * 向队列中添加任务
	 * @param task
	 * @param limitSize 限定worker队列的长度
	 * @param abortNewTask 是否抛弃新的任务 当队列的长度达到定长 true表示抛弃新来的任务 false表示抛弃老的任务
	 * @return true 表示添加task成功 false表示添加task失败
	 */
	void addTask(AsyncTask task, int limitSize, boolean abortNewTask) {
		if (this.taskQueue.size() >= limitSize) {
			if (abortNewTask) {
				//抛弃新来的任务
				task.getHandler().exceptionCaught(new TimeoutException(threadFactoryName+" abort this task, because the queue is full!"));
			} else {
				elimintateOldTask(task);
			}
		} else {
			this.taskQueue.offer(task);
		}
	}
	
	public void start() {
		if (subWokerCount <= 1) {
			subWokerCount = 1;
		}
		exec = Executors.newFixedThreadPool(subWokerCount);
		for (int i = 0; i < subWokerCount ; i++) {
			Thread t = new executeThread();
			t.setName(threadFactoryName + "_" +  i);
			t.setDaemon(true);
			exec.execute(t);
		}
	}
	
	/**
	 * 
	 * @param task
	 * @return
	 */
	private boolean elimintateOldTask(AsyncTask task) {
		AsyncTask oldTask = this.taskQueue.poll();
		oldTask.getHandler().exceptionCaught(new TimeoutException(threadFactoryName+" abort this task, beacuse the queue is full!"));
		try {
			this.taskQueue.offer(task, 1, TimeUnit.MILLISECONDS);
			return true;
		} catch (InterruptedException e) {
			task.getHandler().exceptionCaught(new TimeoutException(threadFactoryName+" The queue is full!"));
			return false;
		}
	}
	
	BlockingQueue<AsyncTask> getTaskQueue() {
		return taskQueue;
	} 
	
	/**
	 * 
	 * @param task
	 * @throws Throwable
	 */
	private void execTimeoutTask(AsyncTask task) throws Throwable  {
		if ((System.currentTimeMillis() - task.getAddTime()) > task.getQtimeout()) {
			task.getHandler().exceptionCaught(new TimeoutException(threadFactoryName+"async task timeout!"));
			return;
		} else {
			Object obj = task.getHandler().run();
			task.getHandler().messageReceived(obj);
		}
	}
	
	/**
	 * 停止工作线程(stop is final)
	 */
	void end() {
		this.isStop = true;
		logger.info("-------------------async workder is stop-------------------");
	}
	
	class executeThread extends Thread {
		
		public executeThread() {
			
		}
		
		public void run() {
			while (!isStop) {
				AsyncTask task = null;
				try {
					task = taskQueue.poll(1500, TimeUnit.MILLISECONDS);
					if(null != task) {
						execTimeoutTask(task);
					}
				} catch(InterruptedException ie) {
					logger.error(ie);
				} catch(Throwable ex) {
					if(task != null) {
						task.getHandler().exceptionCaught(ex);
					}
				}
			}
		}
	}
}
