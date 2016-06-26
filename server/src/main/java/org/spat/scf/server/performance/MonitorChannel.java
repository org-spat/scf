package org.spat.scf.server.performance;

import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;

public class MonitorChannel {
	private Channel channel;
	private SocketAddress socketAddress;
	private Command command;
	
	private int convergeCount;
	private long convergeTime;
	private long beginTime;


	public MonitorChannel() {

	}

	public MonitorChannel(Command command, 
			Channel channel,
			SocketAddress socketAddress) {
		this.setCommand(command);
		this.setChannel(channel);
		this.setSocketAddress(socketAddress);
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public SocketAddress getSocketAddress() {
		return socketAddress;
	}

	public void setSocketAddress(SocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	public Command getCommand() {
		return command;
	}

	public void setConvergeCount(int convergeCount) {
		this.convergeCount = convergeCount;
	}

	public int getConvergeCount() {
		return convergeCount;
	}
	
	public long getConvergeTime() {
		return convergeTime;
	}

	public void setConvergeTime(long convergeTime) {
		this.convergeTime = convergeTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getBeginTime() {
		return beginTime;
	}
}
