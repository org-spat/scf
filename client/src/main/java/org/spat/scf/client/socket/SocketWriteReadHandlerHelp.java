package org.spat.scf.client.socket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

public class SocketWriteReadHandlerHelp {
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public static SocketWriteReadHandlerHelp getInstance(){
		return SocketWriteReadHandlerHelpHolder.handlerHelp;
	}
	
	private static class SocketWriteReadHandlerHelpHolder{
		public static SocketWriteReadHandlerHelp handlerHelp = new SocketWriteReadHandlerHelp();
	}
	
	
	public ScheduledFuture schedule(Runnable command, long delay, TimeUnit unit) {
		return scheduler.schedule(command, delay, unit);
	}
}
