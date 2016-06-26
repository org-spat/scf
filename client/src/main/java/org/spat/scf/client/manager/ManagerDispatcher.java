package org.spat.scf.client.manager;

import java.util.List;

public class ManagerDispatcher {
	private List<ConfigServer> servers;
	private static int count = 0;

	public ManagerDispatcher(String[] ips, int port) {
		ConfigServer cs = null;
		for (String ip : ips) {
			cs = new ConfigServer(ip, port);
			servers.add(cs);
		}
	}

	public ConfigServer GetServer() {
		if (servers == null || servers.size() == 0) {
			return null;
		}
		ConfigServer server = servers.get(count);
		while(!server.isFlag()) {
			server = servers.get(++count);
		}
		
		return server;
	}
}
