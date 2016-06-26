package org.spat.utility.expandasync;


public class AsyncTask {
	/**
	 * 默认超时时间为3秒
	 */
	private static final int DEFAULT_TIME_OUT = 3000;
	
	private int timeout;
	
	private int qtimeout;
	
	private long addTime;
	
	private int inQueueTime;
	
	private IAsyncHandler handler;
	

	/**
	 * 构造异步任务
	 * @param timeout 超时时间(单位：豪秒)
	 * @param handler 执行句柄
	 */
	public AsyncTask(int timeout, IAsyncHandler handler) {
		super();
		if(timeout < 0){
			timeout = 1000;
		}
		this.timeout = timeout;
		this.qtimeout = ((timeout * 3)/2)+1;
		this.handler = handler;
		this.addTime = System.currentTimeMillis();
	}
	
	/**
	 * 构造异步任务
	 * @param timeout 超时时间(单位：豪秒)
	 * @param handler 执行句柄
	 */
	public AsyncTask(int timeout, IAsyncHandler handler, int inQueueTime) {
		super();
		if(timeout < 0){
			timeout = 1000;
		}
		this.timeout = timeout;
		this.qtimeout = ((timeout * 3)/2)+1;
		this.handler = handler;
		this.addTime = System.currentTimeMillis();
		this.inQueueTime = inQueueTime;
	}
	
	/**
	 * 构造异步任务
	 * @param handler 执行句柄
	 */
	public AsyncTask(IAsyncHandler handler) {
		super();
		this.timeout = DEFAULT_TIME_OUT;
		this.qtimeout = ((timeout * 3)/2)+1;
		this.handler = handler;
		this.addTime = System.currentTimeMillis();
	}
	
	
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public IAsyncHandler getHandler() {
		return handler;
	}

	public void setHandler(IAsyncHandler handler) {
		this.handler = handler;
	}

	public long getAddTime() {
		return addTime;
	}

	public int getQtimeout() {
		return qtimeout;
	}

	public void setQtimeout(int qtimeout) {
		this.qtimeout = qtimeout;
	}

	public int getInQueueTime() {
		return inQueueTime;
	}

	public void setInQueueTime(int inQueueTime) {
		this.inQueueTime = inQueueTime;
	}
}
