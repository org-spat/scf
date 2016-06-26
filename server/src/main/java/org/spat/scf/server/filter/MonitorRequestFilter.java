package org.spat.scf.server.filter;

import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.filter.IFilter;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;

/**
 * A filter for set SCFContext monitor true
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class MonitorRequestFilter implements IFilter {

	private static ILog logger = LogFactory.getLogger(MonitorRequestFilter.class);
	
	
	@Override
	public void filter(SCFContext context) throws Exception {
		logger.debug("MonitorRequestFilter set monitor true");
		context.setMonitor(true);
	}


	@Override
	public int getPriority() {
		return 0;
	}

}