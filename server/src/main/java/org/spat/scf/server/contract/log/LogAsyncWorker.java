package org.spat.scf.server.contract.log;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class LogAsyncWorker {
	
	private final AtomicInteger index = new AtomicInteger(0);
	private static final int THREAD_COUNT = 1;
	private static int THREAD_POOL_QUEUE_SIZE = 1024 * 16;
	
	static {
		
		try {
			String countstr = System.getProperty("asyn.log.queue.size");
			int count = 0;
			if (countstr != null && !"".equals(countstr)) {
				count = Integer.parseInt(countstr);
			}
			THREAD_POOL_QUEUE_SIZE = count != 0 ? count : 1024 * 16;
		} catch (NumberFormatException e) {
			THREAD_POOL_QUEUE_SIZE = 1024 * 16;
		}
	}
	
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
	
	private static class LogAsyncWorkerHolder {
		public static LogAsyncWorker subscribeWorker = new LogAsyncWorker();
	}

	public static LogAsyncWorker getLogAsyncWorker() {
		return LogAsyncWorkerHolder.subscribeWorker;
	}
	
	public void execute(final Logger logger, final String callerFQCN, final Level level, final String log){
		logExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				logger.log(level, log);
				if (index.getAndDecrement() % 1000 == 0) {
					try {
						TimeUnit.MILLISECONDS.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public void execute(final Logger logger, final String callerFQCN, final Level level, final String log, final Throwable e){
		logExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				logger.log(level, log, e);
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
	
	public int getSize() {
		return logExecutor.getQueue().size();
	}
	
	public void shutdown() {
		if (logExecutor != null) {
			logExecutor.shutdown();
		}
	}
}
