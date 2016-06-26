package org.spat.utility.async;

public interface IAsyncHandler {
	
	/**
	 * 异步执行的任务
	 * @return
	 */
	public Object run() throws Throwable;
	
	/**
	 * 响应消息到达
	 * @param obj 返回值
	 */
	public void messageReceived(Object obj);
	
	/**
	 * 发生异常
	 * @param e 异常
	 */
	public void exceptionCaught(Throwable e);
	
}
