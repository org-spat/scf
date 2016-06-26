package org.spat.scf.server.filter;

import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.context.StopWatch;
import org.spat.scf.server.contract.filter.IFilter;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.performance.monitor.MonitorCount;
import org.spat.scf.server.performance.monitor.MonitorMethodExecTime;

public class MonitorFilter implements IFilter {

	private static ILog logger = LogFactory.getLogger(MonitorFilter.class);

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void filter(SCFContext context) throws Exception {
		try {
			if (null != context) {
				StopWatch sw = context.getStopWatch();
				if (null != sw) {
					// 将sw发送到Monitor
					MonitorCount.messageRecv(sw);
					MonitorMethodExecTime.messageRecv(sw);
				}
			}
		} catch (Exception mex) {
			logger.info("MonitorCount error" + mex);
		}

	}

}
