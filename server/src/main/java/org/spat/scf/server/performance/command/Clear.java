package org.spat.scf.server.performance.command;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.performance.Command;
import org.spat.scf.server.performance.CommandType;

public class Clear extends CommandHelperBase {
	
	private static boolean stop = false;

	public static void setStop(boolean stop) {
		Clear.stop = stop;
	}
	
	public static boolean isStop() {
		return stop;
	}

	@Override
	public Command createCommand(String commandStr) {
		if(commandStr != null && !commandStr.equalsIgnoreCase("")) {
			if(commandStr.equalsIgnoreCase("clear")) {
				Command entity = new Command();
				entity.setCommandType(CommandType.Clear);
				return entity;
			}
		}
		return null;
	}

	@Override
	public void execCommand(Command command, MessageEvent event)
			throws Exception {
		if(command.getCommandType() == CommandType.Clear) {
			logger.info("clear monitor");
			Clear.setStop(false);
		}
		
	}

	@Override
	public void messageReceived(SCFContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeChannel(Command command, Channel channel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getChannelCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
