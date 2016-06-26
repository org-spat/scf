package org.spat.scf.server.filter;

import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.filter.IFilter;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.performance.MonitorCenter;

/**
 * A filter for add monitor task to MonitorCenter
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class MonitorResponseFilter implements IFilter {

	private static ILog logger = LogFactory.getLogger(MonitorRequestFilter.class);
	
	
	@Override
	public void filter(SCFContext context) throws Exception {
		logger.debug("MonitorResponseFilter addMonitorTask");
		MonitorCenter.addMonitorTask(context);
	}


	@Override
	public int getPriority() {
		return 0;
	}

}