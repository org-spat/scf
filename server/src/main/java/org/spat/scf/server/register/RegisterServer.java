package org.spat.scf.server.register;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.contract.server.IServer;

/**
 * start netty server
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class RegisterServer implements IServer {

	public RegisterServer() {

	}

	static ILog logger = LogFactory.getLogger(RegisterServer.class);

	static ChannelFuture channelFuture;

	static ClientBootstrap bootstrap;

	private static String[] hosts;
	private static int port = 7070;
	private static int selected;
	private static int COUNT;
	private static boolean flag = false;

	static {
		try {
			hosts = Global.getSingleton().getServiceConfig()
					.getString("scf.service.manangement.addr").trim()
					.split(",");
			port = Global.getSingleton().getServiceConfig()
					.getInt("scf.service.manangement.addr.port");
			COUNT = hosts.length;
			selected = new Random().nextInt(COUNT);
		} catch (Exception e) {
			logger.error("get management address error!!", e);
		}
	}

	/**
	 * start netty server
	 */
	@Override
	public void start() throws Exception {
		logger.info("-------register server start --------");
		initSocketServer();
	}

	/**
	 * stop netty server
	 */
	@Override
	public void stop() throws Exception {
		logger.info("-- closing all channels...");
		flag = true;
		channelFuture.getChannel().close().awaitUninterruptibly();
//		channelFuture.awaitUninterruptibly(); // wait for channel close
		logger.info("-- closed all channels...");
		bootstrap.getFactory().releaseExternalResources(); // rele
	}

	/**
	 * 初始化socket server
	 * 
	 * @throws Exception
	 */
	private static void initSocketServer() throws Exception {
		// Parse options.

		logger.info("-- register server config --");
		logger.info("-- hosts:"
				+ Global.getSingleton().getServiceConfig()
						.getString("scf.service.manangement.addr"));
		logger.info("-- selected :" + selected);
		logger.info("-- listen ip: " + hosts[selected % COUNT]);
		logger.info("-- port: "
				+ Global.getSingleton().getServiceConfig()
						.getInt("scf.service.manangement.addr.port"));
		logger.info("-- : connect scf server management...");

		// Configure the client.
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		// Set up the pipeline factory.
		bootstrap.setPipelineFactory(new RegisterPipelineFactory());

		// Start the connection attempt.
		channelFuture = bootstrap.connect(new InetSocketAddress(
				hosts[selected++ % COUNT], port));

		// Wait until the connection is closed or the connection attempt fails.
		channelFuture.awaitUninterruptibly();

		logger.info("connect scf server management end. state:"
				+ channelFuture.isSuccess());
	}

	public ChannelFuture getChannelFuture() {
		return channelFuture;
	}

	@SuppressWarnings("static-access")
	public void setChannelFuture(ChannelFuture channelFuture) {
		this.channelFuture = channelFuture;
	}

	public static boolean reconnect() {
		try {
			if(flag) {
				return false;
			}
			logger.info("server reconnect....");
			selected = testAlive();
			while (selected == -1) {
				selected = testAlive();
			}
			channelFuture = bootstrap.connect(new InetSocketAddress(hosts[selected], port));
			channelFuture.awaitUninterruptibly();
			logger.debug("reconnecct scf server management...");
			return channelFuture.isSuccess();
		} catch (Exception e) {
			return false;
		}
	}

	private static int testAlive() {
		int n = selected++ % COUNT;
		selected = selected % COUNT;
		while (n != selected) {
			try {
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(hosts[selected], port), 100);
				socket.close();
				return selected;
			} catch (Exception e) {
				try {
					selected = (selected + 1) % COUNT;
					Thread.sleep(1000);
					continue;
				} catch (InterruptedException e1) {
				}
			}
		}
		return -1;
	}

	public static void main(String[] args) throws Exception {
		RegisterServer rs = new RegisterServer();
		rs.start();
		// rs.send("hello world");
		Thread.sleep(100000);
	}

}