package org.spat.scf.server.filter;

import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.filter.IFilter;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.utility.IPTable;

public class IPFilter implements IFilter {
	
	private static ILog logger = LogFactory.getLogger(IPFilter.class);

	@Override
	public void filter(SCFContext context) throws Exception {
		if(IPTable.isAllow(context.getChannel().getRemoteIP())) {
			logger.info("new channel conected:" + context.getChannel().getRemoteIP());
		} else {
			logger.error("forbid ip not allow connect:" + context.getChannel().getRemoteIP());
			context.getChannel().close();
		}
	}

	@Override
	public int getPriority() {
		return 100;
	}

}