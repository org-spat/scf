package org.spat.scf.server.performance.command;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.context.StopWatch;
import org.spat.scf.server.performance.Command;
import org.spat.scf.server.performance.CommandType;
import org.spat.scf.server.performance.MonitorCenter;
import org.spat.scf.server.performance.MonitorChannel;

public class Count extends CommandHelperBase {
	
	private static List<MonitorChannel> taskList = new ArrayList<MonitorChannel>();

	@Override
	public Command createCommand(String commandStr) {
		if(commandStr != null && !commandStr.equalsIgnoreCase("")) {
			String[] args = commandStr.split("\\|");
			if(args[0].trim().equalsIgnoreCase("count")) {
				Command entity = new Command();
				entity.setCommandType(CommandType.Count);
				entity.setSecond(1);
				entity.setMethod("#all#");
				if(args.length > 1) {
					for(int i=1; i<args.length; i++) {
						if(args[i].trim().startsWith("second")) {
							entity.setSecond(Integer.parseInt(args[i].trim().replaceFirst("second ", "").trim()));
						} else if(args[i].trim().startsWith("method")) {
							entity.setMethod(args[i].trim().replaceFirst("method ", "").trim());
						}
					}
				}
				return entity;
			}
		}
		return null;
	}

	
	
	@Override
	public void execCommand(Command command, MessageEvent event) throws Exception{
		if(command.getCommandType() == CommandType.Count) {
			MonitorCenter.addFilter(); // add filter
			logger.info("add count monitor channel:" + event.getChannel().getRemoteAddress());
			for(int i=0; i<taskList.size(); i++) {
				if(taskList.get(i).getChannel().equals(event.getChannel()) || !taskList.get(i).getChannel().isOpen()) {
					taskList.remove(i);
				}
			}
			taskList.add(new MonitorChannel(command, event.getChannel(), event.getChannel().getRemoteAddress()));
		}
	}



	@SuppressWarnings("rawtypes")
	@Override
	public void messageReceived(SCFContext context) {
		if(taskList.size() <= 0) {
			return;
		}
		StopWatch sw = context.getStopWatch();
		if (sw != null) {
			Map<String, StopWatch.PerformanceCounter> mapCounter = sw.getMapCounter();
			StringBuilder sbAllMsg = new StringBuilder();
			sbAllMsg.append("#all#");
			Iterator itSW = mapCounter.entrySet().iterator();
			while (itSW.hasNext()) {
				Map.Entry entrySW = (Map.Entry) itSW.next();
				StopWatch.PerformanceCounter pc = (StopWatch.PerformanceCounter) entrySW.getValue();
				sbAllMsg.append("key:");
				sbAllMsg.append(pc.getKey());
			}
		
			try {
				String allMsg = sbAllMsg.toString();
				for (MonitorChannel mc : taskList) {
					long now = System.currentTimeMillis();
					if((now - mc.getBeginTime()) > (mc.getCommand().getSecond() * 1000)) {
						String msg = mc.getCommand().getMethod() + "  " + String.valueOf(mc.getConvergeCount()) + "\r\n";
						byte[] responseByte = msg.getBytes("utf-8");
						mc.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
						mc.setBeginTime(now);
						mc.setConvergeCount(0);
					} else {
						if(allMsg.indexOf(mc.getCommand().getMethod()) >= 0) {
							mc.setConvergeCount(mc.getConvergeCount() + 1);
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("send monitor data", e);
			}
		}
	}



	@Override
	public void removeChannel(Command command, Channel channel) {
		if(command.getCommandType() != CommandType.Count) {
			for(int i=0; i<taskList.size(); i++) {
				if(taskList.get(i).getChannel().equals(channel) || !taskList.get(i).getChannel().isOpen()) {
					taskList.remove(i);
				}
			}
		}
	}
	
	@Override
	public int getChannelCount() {
		for(int i=0; i<taskList.size(); i++) {
			if(!taskList.get(i).getChannel().isOpen()) {
				taskList.remove(i);
			}
		}
		return taskList.size();
	}
}