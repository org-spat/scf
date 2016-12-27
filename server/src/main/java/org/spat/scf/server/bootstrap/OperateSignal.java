package org.spat.scf.server.bootstrap;

import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.ServerStateType;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class OperateSignal implements SignalHandler{
	
	private static ILog logger = LogFactory.getLogger(OperateSignal.class);

	@Override
	public void handle(Signal signalName) {
		//设置当前服务状态为重启
		Global.getSingleton().setServerState(ServerStateType.Reboot);
		logger.info(Global.getSingleton().getServiceConfig().getString("scf.service.name")+" Server state is "+Global.getSingleton().getServerState());
		logger.info(Global.getSingleton().getServiceConfig().getString("scf.service.name")+" Server will reboot!");
	}
}
