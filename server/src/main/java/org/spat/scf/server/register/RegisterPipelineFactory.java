package org.spat.scf.server.register;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

/**
 * netty Pipeline Factory
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class RegisterPipelineFactory implements ChannelPipelineFactory {
    final int firstMessageSize = 1024;

	public ChannelPipeline getPipeline() throws Exception {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = pipeline();
//		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(firstMessageSize, true));
//		// and then business logic.
		pipeline.addLast("handler", new RegisterHandler(firstMessageSize));
//		pipeline.addLast("timeout", new IdleStateHandler(new Timer(), 10, 10, 0));//此两项为添加心跳机制 10秒查看一次在线的客户端channel是否空闲，IdleStateHandler为netty jar包中提供的类
		return pipeline;
	}
}