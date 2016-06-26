package org.spat.scf.server.communication.tcp;

import java.nio.ByteBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.spat.scf.protocol.utility.ProtocolConst;
import org.spat.scf.protocol.utility.ProtocolHelper;
import org.spat.scf.server.contract.context.ApproveContext;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.context.SCFChannel;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.context.SecureContext;
import org.spat.scf.server.contract.context.ServerType;
import org.spat.scf.server.contract.filter.IFilter;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.contract.server.IServerHandler;
import org.spat.scf.server.utility.ExceptionHelper;
//import org.spat.scf.server.performance.monitorweb.MonitorErrorLog;

/**
 * netty event handler
 * 
 * @author Service Platform Architecture Team
 * 
 * 
 */
@ChannelPipelineCoverage("all")
public class SocketHandler extends SimpleChannelUpstreamHandler implements IServerHandler {
	
	private static ILog logger = LogFactory.getLogger(SocketHandler.class);
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		try {
			logger.debug("message receive");
			ByteBuffer buffer = ((ChannelBuffer)e.getMessage()).toByteBuffer(); 
			byte[] reciveByte = buffer.array();
			logger.debug("reciveByte.length:" + reciveByte.length);
			// check head delimiter  
			byte[] headDelimiter = new byte[ProtocolConst.P_START_TAG.length];
			System.arraycopy(reciveByte, 0, headDelimiter, 0, ProtocolConst.P_START_TAG.length);
			
			if(ProtocolHelper.checkHeadDelimiter(headDelimiter)) {
				byte[] requestBuffer = new byte[reciveByte.length - ProtocolConst.P_START_TAG.length];
				System.arraycopy(reciveByte, ProtocolConst.P_START_TAG.length, requestBuffer, 0, (reciveByte.length - ProtocolConst.P_START_TAG.length));
				
				SCFContext scfContext = new SCFContext(requestBuffer,
						new SCFChannel(e.getChannel()), 
						ServerType.TCP,
						this);
				
				SocketServer.invokerHandle.invoke(scfContext);
			} else {
				byte[] response = ExceptionHelper.createErrorProtocol();
				e.getChannel().write(ChannelBuffers.copiedBuffer(response));
				logger.error("protocol error: protocol head not match");
			}
		} catch(Throwable ex) {
			byte[] response = ExceptionHelper.createErrorProtocol();
			e.getChannel().write(response);
			logger.error("SocketHandler invoke error", ex);			
			
		}
	}
	
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof ChannelStateEvent) {
			logger.debug(e.toString());
		}
		super.handleUpstream(ctx, e);
	}
	
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		SocketServer.allChannels.add(e.getChannel());
		logger.info("new channel open:" + e.getChannel().getRemoteAddress().toString());
		/**
		 * 如果当前服务启动权限认证,则增加当前连接对应的SecureContext
		 * @author HaoXB
		 */
		if(Global.getSingleton().getGlobalSecureIsRights()){// Channel
			Global.getSingleton().addChannelMap(e.getChannel(), new SecureContext());
		}
		
		if(Global.getSingleton().getApproveIsRights()) {
			Global.getSingleton().addChannelApproveMap(e.getChannel(), new ApproveContext());
		}
		
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		for(IFilter filter : Global.getSingleton().getConnectionFilterList()) {
			filter.filter(new SCFContext(new SCFChannel(e.getChannel())));
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("unexpected exception from downstream remoteAddress(" + 
					e.getChannel().getRemoteAddress().toString() + 
					")",
					e.getCause());
		
//		e.getChannel().close();
	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e){
		logger.info("channel is closed:" + e.getChannel().getRemoteAddress().toString());
		e.getChannel().close();
		
		/**
		 * 如果当前服务启动权限认证,则删除当前连接对应的SecureContext
		 * @author HaoXB
		 */
		if(Global.getSingleton().getGlobalSecureIsRights()){
			Global.getSingleton().removeChannelMap(e.getChannel());
		}
		
		if(Global.getSingleton().getApproveIsRights()) {
			Global.getSingleton().removeChannelApproveMap(e.getChannel());
		}
	}

	
	@Override
	public void writeResponse(SCFContext context) {
		if(context != null && context.getScfResponse() != null){
			context.getChannel().write(context.getScfResponse().getResponseBuffer());
		} else {
			context.getChannel().write(new byte[]{0});
			logger.error("context is null or response is null in writeResponse");
		}
	}
}