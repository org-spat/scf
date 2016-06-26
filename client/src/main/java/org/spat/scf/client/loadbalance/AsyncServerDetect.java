package org.spat.scf.client.loadbalance;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;
import org.spat.scf.client.utility.AsyncDetectHelper;

public class AsyncServerDetect {
	
	private DetectWork detectWorker = null;

	private static class AsyncServerDetectHolder {
		public static AsyncServerDetect instance = new AsyncServerDetect();
	}

	public static AsyncServerDetect getInstance() {
		return AsyncServerDetectHolder.instance;
	}

	private AsyncServerDetect() {
		detectWorker = new DetectWork();
		Thread thread = new Thread(detectWorker);
		thread.setName("ServerDetect-thread");
		thread.setDaemon(true);
		thread.start();
	}

	public void add(Server s) {
		detectWorker.add(s);
	}
}

class DetectWork implements Runnable {
	private static ILog logger = LogFactory.getLogger(DetectWork.class);
	private List<Server> servers = new LinkedList<Server>();
	private ConcurrentLinkedQueue<Server> newServers= new ConcurrentLinkedQueue<Server>();
	
	@Override
	public void run() {
		while (true) {
			try {
				if(!newServers.isEmpty()) {
					for (Iterator<Server> iterator = newServers.iterator(); iterator.hasNext();) {
						Server serv = (Server) iterator.next();
						servers.add(serv);
						iterator.remove();
					}
				}
				if (servers.size() > 0) {
					for (Iterator<Server> iterator = servers.iterator(); iterator.hasNext();) {
						Server serv = (Server) iterator.next();
						if ((serv.getState() == ServerState.Dead || serv.getState() == ServerState.Reboot)
								&& (System.currentTimeMillis() - serv.getDeadTime()) > serv.getDeadTimeout()) {
							if (AsyncDetectHelper.test(serv)) {
								/**
								 * 初始化server 状态字段
								 */
								serv.setDeadTime(0);
								serv.initServer();
								serv.setWeight(5);
								serv.setState(ServerState.Normal);
								iterator.remove();
								logger.info("Time " + System.currentTimeMillis() + " server " + serv.getAddress() + "is activated！！");
							} else {
								serv.setDeadTime(System.currentTimeMillis());
							}
						} else if (serv.getState() == ServerState.Deleted || serv.getState() == ServerState.Normal) {
							iterator.remove();
							logger.debug("Time " + System.currentTimeMillis() + " server " + serv.getAddress() + "is remove from the queue！！");
						}
					}
				}
				Thread.sleep(5000);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	public void add(Server server) {
		newServers.offer(server);
	}
}
