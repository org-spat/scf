package org.spat.scf.server.contract.context;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.spat.scf.protocol.utility.ProtocolConst;
import org.spat.scf.server.utility.ExceptionHelper;

public class SCFChannel {
	
	private Channel nettyChannel;
	private String remoteIP;
	private int remotePort;
	private String localIP;
	private int localPort;

	public SCFChannel() {
		
	}
	
	public SCFChannel(Channel nettyChannel) {
		super();
		this.nettyChannel = nettyChannel;
		SocketAddress remoteAddress = nettyChannel.getRemoteAddress();
		this.remoteIP = ((InetSocketAddress)remoteAddress).getAddress().getHostAddress();
		this.remotePort = ((InetSocketAddress)remoteAddress).getPort();
		SocketAddress localAddress = nettyChannel.getLocalAddress();
		this.localIP = ((InetSocketAddress)localAddress).getAddress().getHostAddress();
		this.localPort = ((InetSocketAddress)localAddress).getPort();
	}

	public void close() {
		nettyChannel.close();
	}
	
	public void write(byte[] buffer) {
		if(buffer == null) {
			buffer = ExceptionHelper.createErrorProtocol();
		}
		this.nettyChannel.write(ChannelBuffers.copiedBuffer(ProtocolConst.P_START_TAG, buffer, ProtocolConst.P_END_TAG));	
	}
	
	public Channel getNettyChannel() {
		return nettyChannel;
	}

	public String getRemoteIP() {
		return remoteIP;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public String getLocalIP() {
		return localIP;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setNettyChannel(Channel nettyChannel) {
		this.nettyChannel = nettyChannel;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public void setLocalIP(String localIP) {
		this.localIP = localIP;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}
}
