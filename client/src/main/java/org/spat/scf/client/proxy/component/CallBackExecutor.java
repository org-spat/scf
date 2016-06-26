package org.spat.scf.client.proxy.component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.spat.scf.client.socket.ThreadRenameFactory;
import org.spat.scf.client.utility.SystemUtils;

public class CallBackExecutor {
	
	final static ThreadPoolExecutor callBackExe = new ThreadPoolExecutor(SystemUtils.getSystemThreadCount(), 
			SystemUtils.getSystemThreadCount(), 1500L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadRenameFactory("CallBackExecutor-Thread"));
}
