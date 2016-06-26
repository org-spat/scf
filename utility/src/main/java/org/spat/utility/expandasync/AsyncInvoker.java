package org.spat.utility.expandasync;

public class AsyncInvoker {
	
	/**
	 * Round Robin
	 */
	private int rr = 0;
	
	/**
	 * 工作线程
	 */
	private AsyncWorker[] workers = null;
	
	private int limitSize = 0;
	private boolean mode = false;
	
	public static AsyncInvoker getInstance() {
		//获取CPU个数
		int cpuCount = Runtime.getRuntime().availableProcessors();
		return new AsyncInvoker(cpuCount, 1, 0, false, false, null);
	}
	
	public static AsyncInvoker getInstance(int workerCount, int limitSize, boolean isSteal, boolean mode, String threadFactoryName) {
		return new AsyncInvoker(workerCount, 1, limitSize, isSteal, mode, threadFactoryName);
	}
	
	public static AsyncInvoker getInstance(int workerCount, int subWorkerCount,  int limitSize, boolean isSteal, boolean mode, String threadFactoryName) {
 		return new AsyncInvoker(workerCount, subWorkerCount, limitSize, isSteal, mode, threadFactoryName);
	}
	
	public AsyncInvoker(int workerCount, int subWorkerCount, int limitSize, boolean isSteal, boolean mode, 
			String threadFactoryName) {
		if(null == threadFactoryName){
			threadFactoryName = "";
		}
		
		workers = new AsyncWorker[workerCount];
		
		this.limitSize = limitSize;
		this.mode = mode;
		
		for(int i=0; i<workers.length; i++) {
			workers[i] = new AsyncWorker(subWorkerCount, limitSize, threadFactoryName);
			workers[i].start();
		}
	}

	/**
	 * 执行异步任务(无阻塞立即返回,当前版本只实现轮询分配,下个版本增加工作线程间的任务窃取)
	 * @param timeOut 超时时间
	 * @param handler 任务handler
	 */
	public void run(int timeOut, IAsyncHandler handler) {
		AsyncTask task = new AsyncTask(timeOut, handler);
		balanceTask(task);
	}
	
	/**
	 * 执行异步任务(无阻塞立即返回,当前版本只实现轮询分配,下个版本增加工作线程间的任务窃取)
	 * @param timeOut 超时时间
	 * @param inQueue 超过此时间，则打印任务在队列中的时间
	 * @param handler 任务handler
	 */
	public void run(int timeOut, int inQueue, IAsyncHandler handler) {
		AsyncTask task = new AsyncTask(timeOut, handler, inQueue);
		balanceTask(task);
	}
	
	public void balanceTask(AsyncTask task) {
		if(rr > 10000) {
			rr = 0;
		}
		int idx = rr % workers.length;
		if (limitSize > 0) {
			workers[idx].addTask(task, limitSize, mode);
		} else {
			workers[idx].addTask(task);
		}
		++rr;
	}
	
	/**
	 * 停止所有工作线程
	 */
	public void stop() {
		for(AsyncWorker worker : workers) {
			worker.end();
		}
	}
}
