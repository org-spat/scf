
package org.spat.scf.client.loadbalance;

import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;
import org.spat.scf.client.socket.ScoketPool;

/**
 * TimerJob
 *
 * @author Service Platform Architecture Team 
 */
public class TimerJob implements Runnable {
	
	private final static ILog logger = LogFactory.getLogger(TimerJob.class);
	private Server server = null;
	
	public TimerJob(Server server){
		super();
		this.server = server;
	}
	
	@Override
	public void run() {
		/**
    	 * 如果当前连接处于重启状态则注销当前服务所有socket
    	 */
		try{
			ScoketPool sp = server.getScoketpool();
			try {
    			sp.destroy();
    		} catch (Throwable e) {
    			logger.info("destroy socket fail!");
    			logger.error(e);
    		}
		}catch(Exception ex){
			logger.error(ex);
		} catch (Throwable e) {
			logger.error(e);
		}
	}

}
