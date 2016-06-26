package org.spat.utility.async;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LogAsyncWorker {
	
	private static final Log logger = LogFactory.getLog(LogAsyncWorker.class);
	private final AtomicInteger index = new AtomicInteger(0);
	private static final int THREAD_COUNT = 1;
	private static final int THREAD_POOL_QUEUE_SIZE = 1024 * 16;
	
	/**
	 * 固定队列长度线程池,当超过队列长度时淘汰最老的数据(直接淘汰不输出日志)
	 */
	private static final ThreadPoolExecutor logExecutor = new ThreadPoolExecutor(
			THREAD_COUNT, 
			THREAD_COUNT, 
			1000 * 60,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(THREAD_POOL_QUEUE_SIZE),
            new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "LogAsyncWorker_" + this.threadIndex.incrementAndGet());
                }
            },
            new ThreadPoolExecutor.DiscardOldestPolicy());
	
	private static class LogAsyncWorkerHolder{
		public static LogAsyncWorker subscribeWorker = new LogAsyncWorker();
	}
	
	public static LogAsyncWorker getLogAsyncWorker(){
		return LogAsyncWorkerHolder.subscribeWorker;
	}
	
	public void execute(final String log){
		logExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				logger.error(log);
				if(index.getAndDecrement() % 1000 == 0){
					try {
						TimeUnit.MILLISECONDS.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public void execute(final String log, final Throwable e){
		logExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				logger.error(log, e);
				if(index.getAndDecrement() % 1000 == 0){
					try {
						TimeUnit.MILLISECONDS.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public int getSize(){
		return this.logExecutor.getQueue().size();
	}
	
	public void shutdown(){
		if(this.logExecutor != null){
			this.logExecutor.shutdown();
		} 
	}
}
