package org.spat.scf.client.manager;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;
import org.spat.scf.client.socket.ManagerSocket;
import org.spat.scf.client.utility.LinkedTransferQueue;
import org.spat.scf.client.utility.TransferQueue;

public class CheckServerUtil {
	private static ILog log = LogFactory.getLogger(CheckServerUtil.class);

	private final TransferQueue<ConfigServer> checkQueue = new LinkedTransferQueue<ConfigServer>();
	int taskTimeOut = 20000;
	private final int checkInterval = 5000;
	static Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public void offer(ConfigServer server) {
		checkQueue.add(server);
	}

	public CheckServerUtil() {
		executor.execute(new ServerCheckHandle());
	}

	class ServerCheckHandle implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					ConfigServer cs = checkQueue.poll(1500, TimeUnit.MILLISECONDS);
					if (cs != null && !cs.isFlag()) {
						 try {
					            Socket socket = new Socket();
					            socket.connect(new InetSocketAddress(cs.getIp(), cs.getPort()), 100);
					            socket.close();
					            cs.setFlag(true);
					            ManagerSocket socket1 = new ManagerSocket(cs);
					            cs.setSocket(socket1);				            
					        } catch (Exception e) {
					        	offer(cs);
					        	Thread.sleep(checkInterval);
					        	log.error("server :["+ cs.getIp() + ":" + cs.getPort() + "] cannot connected !!" );
					        } 
					}
					Thread.sleep(10);

				} catch (Exception e) {
					log.error(e);
				}
			}

		}

	}
}
