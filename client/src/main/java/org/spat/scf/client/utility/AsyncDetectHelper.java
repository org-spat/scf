package org.spat.scf.client.utility;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.spat.scf.client.loadbalance.AsyncServerDetect;
import org.spat.scf.client.loadbalance.Server;
import org.spat.scf.client.loadbalance.ServerState;
import org.spat.scf.client.loadbalance.TimerJob;
import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;

public class AsyncDetectHelper {
	private static ILog logger = LogFactory.getLogger(AsyncDetectHelper.class);

    public static void detectDeadServer(Server server) throws Exception {
    	logger.info("markAsDead server:" + server.getState() + "--server hashcode:" + server.hashCode() + "--conn count:" + server.getScoketpool().count());
    	if (server.getState() == ServerState.Dead) {
            logger.info("before markAsDead the server is dead!!!");
            return;
        }
    	synchronized (server) {
    		if (server.getState() == ServerState.Dead) {
                logger.info("before markAsDead the server is dead!!!");
                return;
            }
	    	server.setState(ServerState.Dead);
	    	server.setDeadTime(System.currentTimeMillis());
	    	server.setWeight(-1);
	    	logger.warn("this server is dead!host:" + server.getAddress());
	    	server.getScoketpool().destroy();
	    	AsyncServerDetect.getInstance().add(server);
    	}
    }
    
    public static void detectRebootServer(Server server) {
    	logger.info("markAsReboot server:" + server.getState() + "--server hashcode:" + server.hashCode());
    	if (server.getState() == ServerState.Reboot) {
        	logger.info("before markAsReboot the server is Reboot!");
            return;
        }
    	synchronized(server){
    		if (server.getState() == ServerState.Reboot) {
            	logger.info("before markAsReboot the server is Reboot!");
                return;
            }
    		
    		logger.warn("this server is reboot! host:" + server.getAddress());
    		server.setState(ServerState.Reboot);//设置当前服务为重启状态
    		server.setDeadTime(System.currentTimeMillis());
    		server.setWeight(-1);
            /**
             * 如果当前连接处于重启状态则注销当前服务所有socket
             * 任务调度 3秒后执行
             */
        	server.getScheduler().schedule(new TimerJob(server), 3, TimeUnit.SECONDS);
        	AsyncServerDetect.getInstance().add(server);
    	}
    }
    
	public static boolean test(Server server) {
		String address = server.getAddress();
		int port = server.getPort();
		boolean result = false;
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(server.getAddress(), server.getPort()), 100);
			socket.close();
			result = true;
		} catch (Exception e) {

		} finally {
			logger.info("test server :" + address + ":" + port + "--alive:" + result);
		}
		return result;
	}
}
