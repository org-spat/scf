package org.spat.scf.client.socket;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;

public class SocketWriteReadHandler {
	
	private static final ILog logger = LogFactory.getLogger(SocketWriteReadHandler.class);
	private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
	volatile long lastReadTime;
	volatile long lastWriteTime;
	private final long timeoutNanos;
	private volatile ScheduledFuture<?> timeout;
	private volatile boolean closed;
	
	public SocketWriteReadHandler(long timeout, TimeUnit unit){
		if (unit == null) {
            throw new NullPointerException("unit");
        }

        if (timeout <= 0) {
            timeoutNanos = 0;
        } else {
            timeoutNanos = Math.max(unit.toNanos(timeout), MIN_TIMEOUT_NANOS);
        }
	}
	
	public void channelAdded(CSocket socket) throws Exception {
        if (socket.connecting()) {
            initialize(socket);
        }
    }
	
	public void channelRemoved(CSocket socket) throws Exception {
        destroy();
    }
	
	public void channelRead(CSocket socket) throws Exception {
        lastReadTime = System.nanoTime();
    }
	
	public void channelWrite(CSocket socket) throws Exception {
		lastWriteTime = System.nanoTime();
    }
	
	private void initialize(CSocket socket) {
        lastReadTime = System.nanoTime();
        lastWriteTime = System.nanoTime();
        if (timeoutNanos > 0) {
        	timeout = SocketWriteReadHandlerHelp.getInstance().schedule(new ReadWriteTimeoutTask(socket), timeoutNanos, TimeUnit.NANOSECONDS);
        }
    }
	
	private void destroy() {
        if (timeout != null) {
        	logger.info("this csocket is close.this writeReadHandler will destory.");
            timeout.cancel(false);
            timeout = null;
        }
    }
	
	protected void writeReadTimedOut(CSocket socket) throws Exception {
        if (!closed) {
        	logger.info("this csocket is writeReadTimeOut."+socket.toString());
        	socket.closeAndDisponse();
            closed = true;
        }
    }
	
	protected long getLastTime(){
		return Math.min(lastReadTime, lastWriteTime);
	}
	
	private final class ReadWriteTimeoutTask implements Runnable {

        private final CSocket socket;

        ReadWriteTimeoutTask(CSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            if (!socket.connecting()) {
                return;
            }

            long currentTime = System.nanoTime();
            long nextDelay = timeoutNanos - (currentTime - getLastTime());
            
            if (nextDelay <= 0) {
                try {
                	if(!socket.ping()){
                		writeReadTimedOut(socket);
                	}else{
                		logger.debug("当前连接正常..............");
                		timeout = SocketWriteReadHandlerHelp.getInstance().schedule(this, timeoutNanos, TimeUnit.NANOSECONDS);
                	}
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else {
            	timeout = SocketWriteReadHandlerHelp.getInstance().schedule(this, timeoutNanos, TimeUnit.NANOSECONDS);
            }
        }
    }
	
}
