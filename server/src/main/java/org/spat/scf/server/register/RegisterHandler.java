package org.spat.scf.server.register;

import java.io.ByteArrayOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.spat.scf.protocol.utility.ByteConverter;
import org.spat.scf.protocol.utility.ProtocolConst;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;

/**
 * netty event handler
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
@ChannelPipelineCoverage("all")
public class RegisterHandler extends SimpleChannelUpstreamHandler {

	static ILog logger = LogFactory.getLogger(RegisterHandler.class);

	private final ChannelBuffer firstMessage;
	private static String serviceName;
	private static int port;
	private static int telnetport;
	static {

		try {
			serviceName = Global.getSingleton().getServiceConfig()
					.getString("scf.service.name");
			port = Global.getSingleton().getServiceConfig()
					.getInt("scf.server.tcp.listenPort");
			telnetport = Global.getSingleton().getServiceConfig()
					.getInt("scf.server.telnet.listenPort");
			if (telnetport == 0) {
				telnetport = port + 10000;
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public RegisterHandler(int firstMessageSize) {
		if (firstMessageSize <= 0) {
			throw new IllegalArgumentException("firstMessageSize: "
					+ firstMessageSize);
		}
		firstMessage = ChannelBuffers.buffer(firstMessageSize);
		for (int i = 0; i < firstMessage.capacity(); i++) {
			firstMessage.writeByte((byte) i);
		}
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		SSMReqProtocol rp = null;
		RegisterEntiy rentiy = null;
		try {
			rentiy = new RegisterEntiy(serviceName, port, telnetport, 1);
			rp = new SSMReqProtocol(1, rentiy.toBuffer());
			byte[] data = rp.dataCreate();
			ByteArrayOutputStream stream = null;
			try {
				stream = new ByteArrayOutputStream();
				stream.write(ProtocolConst.P_START_TAG);
				stream.write(ByteConverter
						.intToBytesBigEndian(ProtocolConst.P_START_TAG.length
								+ 4 + data.length));
				stream.write(data);
			} catch (Exception e2) {
				logger.debug("create sendBuffer error.");
				throw e2;
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (Exception e3) {
						throw e3;
					}
				}
			}
			ChannelBuffer cb = ChannelBuffers.wrappedBuffer(stream
					.toByteArray());
			e.getChannel().write(cb);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		// Send back the received message to the remote peer.
		e.getChannel().write(e.getMessage());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.debug(e.toString());

	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		RegisterServer.reconnect();
		e.getChannel().close();
	}
}