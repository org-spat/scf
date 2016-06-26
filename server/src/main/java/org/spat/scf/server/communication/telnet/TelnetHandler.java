package org.spat.scf.server.communication.telnet;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.performance.MonitorCenter;

/**
 * netty event handler
 * 
 * @author Service Platform Architecture Team 
 * 
 */
@ChannelPipelineCoverage("all")
public class TelnetHandler extends SimpleChannelUpstreamHandler {
	
	private static ILog logger = LogFactory.getLogger(TelnetHandler.class);
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		try {
			logger.debug("control message receive");
			MonitorCenter.messageReceived(e);
		} catch(Exception ex) {
			logger.error("control msg error", ex);
		}
	}
	
	
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof ChannelStateEvent) {
			logger.info("control event--"+e.toString());
		}
		super.handleUpstream(ctx, e);
	}
	
	
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		TelnetServer.allChannels.add(e.getChannel());
		logger.info("new control channel open:" + e.getChannel().getRemoteAddress().toString());
	}

	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.info("new control channel conected:" + e.getChannel().getRemoteAddress().toString());
	}

	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("control channel exception(" + 
					e.getChannel().getRemoteAddress().toString() + 
					")",
					e.getCause());
		MonitorCenter.removeChannel(e.getChannel());
		e.getChannel().close();
	}
	
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e){
		logger.info("channel is closed:" + e.getChannel().getRemoteAddress().toString());
		MonitorCenter.removeChannel(e.getChannel());
		e.getChannel().close();
	}
}