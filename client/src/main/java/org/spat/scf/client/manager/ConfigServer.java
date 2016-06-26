package org.spat.scf.client.manager;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;
import org.spat.scf.client.socket.ManagerSocket;

public class ConfigServer {
	//
	static ILog logger = LogFactory.getLogger(ConfigServer.class);
	private String ip;
	private int port;
	private boolean flag = false;
	private boolean testing = false;

	private ManagerSocket socket;

	public ConfigServer(String ip, int port) {
		this.ip = ip;
		this.port = port;
		try {
			this.socket = new ManagerSocket(this);
			if (this.socket.is_connecting()) {
				flag = true;
			} else {
				flag = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
	}

	private void markDead() {
		if (!this.flag) {
			return;
		}

		this.flag = false;
		this.socket.close();
	}

	private boolean test() {
		if (testing) {
			return true;
		}
		testing = true;
		boolean result = false;
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(this.ip, this.port), 100);
			socket.close();
			result = true;
		} catch (Exception e) {
		} finally {
			logger.info("test server :" + this.ip + ":" + this.port
					+ "--alive:" + result);
			testing = false;
		}
		return result;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public ManagerSocket getSocket() {
		return socket;
	}

	public void setSocket(ManagerSocket socket) {
		this.socket = socket;
	}

}
