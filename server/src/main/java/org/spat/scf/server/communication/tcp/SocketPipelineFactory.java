package org.spat.scf.server.communication.tcp;

import static org.jboss.netty.buffer.ChannelBuffers.directBuffer;
import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;

import org.spat.scf.protocol.utility.ProtocolConst;

/**
 * netty Pipeline Factory
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class SocketPipelineFactory implements ChannelPipelineFactory {
	
	private final ChannelHandler handler;
	private int frameMaxLength;

	public SocketPipelineFactory(ChannelHandler handler, int frameMaxLength) {
		this.handler = handler;
		this.frameMaxLength = frameMaxLength;
	}

	public ChannelPipeline getPipeline() throws Exception {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = pipeline();

		// Add the text line codec combination first,
		ChannelBuffer buf = directBuffer(ProtocolConst.P_END_TAG.length);
		buf.writeBytes(ProtocolConst.P_END_TAG);
		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(this.frameMaxLength, true, buf));
		
//		pipeline.addLast("decoder", new StringDecoder(Charset.forName("UTF-8")));
//		pipeline.addLast("encoder", new StringEncoder(Charset.forName("UTF-8")));

		// and then business logic.
		pipeline.addLast("handler", handler);

		return pipeline;
	}
}