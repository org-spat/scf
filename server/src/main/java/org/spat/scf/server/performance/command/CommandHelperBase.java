package org.spat.scf.server.performance.command;

import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;

public abstract class CommandHelperBase implements ICommandHelper {
	
	protected static ILog logger = LogFactory.getLogger(CommandHelperBase.class);

}
