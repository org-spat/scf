package org.spat.scf.client.loadbalance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.spat.scf.client.configurator.ServerProfile;
import org.spat.scf.client.configurator.ServiceConfig;
import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;
import org.spat.scf.client.socket.ScoketPool;

/**
 * Dispatcher
 * 
 * @author Service Platform Architecture Team 
 */
public class Dispatcher {

	private static ILog logger = LogFactory.getLogger(Server.class);
	private List<Server> ServerPool = new ArrayList<Server>();
	private AtomicInteger requestCount = new AtomicInteger(0);

	/**
	 * Constructor
	 * 
	 * @param Configuration
	 *            .Configuration object
	 */
	public Dispatcher(ServiceConfig config) throws Exception {
		logger.info("starting init servers");
		logger.debug("init connection begin:" + System.currentTimeMillis());
		for (ServerProfile ser : config.getServers()) {
			if (ser.getWeithtRate() > 0) {
				Server s = new Server(ser);
				if (s.getState() != ServerState.Disable) {
					ScoketPool sp = new ScoketPool(s, config);
					s.setScoketpool(sp);
					ServerPool.add(s);
				}
			}
		}
		// 添加
		UpdateServer.addAllServerPool(ServerPool);

		logger.debug("init connection end:" + System.currentTimeMillis());
		logger.info("init servers end");
	}

	/**
	 * get Server from Server pool
	 * 
	 * @return return a Server minimum of current user
	 */
	public Server GetServer() {
		if (ServerPool == null || ServerPool.size() == 0) {
			return null;
		}
		Server result = null;

		int count = ServerPool.size();// server num

		int start = requestCount.getAndIncrement() % count;
		if (requestCount.get() > (10 * count - 1)) {
			requestCount.set(requestCount.get() % count);
		}

		for (int i = start; i < start + count; i++) {
			int index = i % count;
			Server server = ServerPool.get(index);
			// 新添加 start
			// 对是否获取server进行判断
			// 获得当前server的权值
			int weight = server.getWeight();
			if (server.getState() == ServerState.Dead || server.getState() == ServerState.Reboot || weight <= 0) {
				requestCount.getAndIncrement();
				continue;
			}

			int temp;
			if (server.getTimesCount() > (10 * count - 1)) {
				temp = server.timesCountGetAndSet(server.getTimesCount() % count);
			} else {
				// 将timesCount++
				temp = server.timesCountGetAndIncrement();
			}

			if (weight < 10 && weight > -1) {
				byte[] abandonArray = server.getAbandonArray();
				// abandonTimes[i] == 1表示server不接受该次请求
				if (abandonArray[temp % server.getAbandonArray().length] == 1) {
					requestCount.getAndIncrement();
					continue;
				}
			}

			if (server.getState() == ServerState.Normal) {
				result = server;
				break;
			}

			requestCount.getAndIncrement();

		}

		// 新添加 如果未获得到server 就在非休眠的server中随机找出一个server
		if (result == null) {
			for (int i = start; i < start + count; i++) {
				int index = i % count;
				Server server = ServerPool.get(index);
				int weight = server.getWeight();
				if (server.getState() != ServerState.Dead && server.getState() != ServerState.Reboot && weight > -1) {
					result = server;
				}
			}
		}
		
		if (result != null) {
			result.setRequestTimes(result.getRequestTimes() + 1);
        }
		
		return result;
	}

	/**
	 * 根据特定服务器集合ServerChoose.serverName[]选中服务器
	 * 
	 * */
	public Server GetServer(ServerChoose sc) {
		if (ServerPool == null || ServerPool.size() == 0) {
			return null;
		}
		Server result = null;

		int count = sc.getServiceCount();// server num
		int start = requestCount.getAndIncrement() % count;

		if (requestCount.get() > (10 * count - 1)) {
			requestCount.set(requestCount.get() % count);
		}

		for (int i = start; i < start + count; i++) {
			int index = i % count;
			Server server = this.GetServer(sc.getServerName()[index]);

			// 新添加 start
			// 对是否获取server进行判断
			// 1)获得当前server的权值
			int weight = server.getWeight();
			if (server.getState() == ServerState.Dead || server.getState() == ServerState.Reboot || weight <= 0) {
				requestCount.getAndIncrement();
				continue;
			}
			// 2)对得到的权值进行分析
			int temp;
			if (server.getTimesCount() > (10 * count - 1)) {
				temp = server.timesCountGetAndSet(server.getTimesCount() % count);
			} else {
				// 将timesCount++
				temp = server.timesCountGetAndIncrement();
			}

			if (weight < 10 && weight > -1) {
				byte[] abandonTimes = server.getAbandonArray();
				// abandonTimes[i] == 1表示server不接受该次请求
				if (abandonTimes[temp % server.getAbandonArray().length] == 1) {
					requestCount.getAndIncrement();
					continue;
				}
			}

			if (server.getState() == ServerState.Normal) {
				result = server;
				break;
			}

			requestCount.getAndIncrement();
		}
		if (result == null) {
			if (ServerPool.size() - sc.getServiceCount() == 0) {
				result = ServerPool.get(new Random().nextInt(count));
				while (result.getWeight() <= 0) {
					result = ServerPool.get(new Random().nextInt(count));
				}
			} else {
				int counts = requestCount.get() % (ServerPool.size() - sc.getServiceCount());
				result = this.GetServer(getNoName(sc.getServerName())[counts]);
			}

			logger.warning("Not get Specified server, This server is "
					+ result.getState() + " DeadTime:" + result.getDeadTime()
					+ " DeadTimeout" + result.getDeadTimeout());
		}

		// 新添加
		// server的总访问次数+ 1
		result.setRequestTimes(result.getRequestTimes() + 1);
		return result;
	}

	public static void closeUpdate() {
		try {
			UpdateServer.closeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 添加 结束

	public void addServer(Server server) {
		ServerPool.add(server);
	}

	public void removeServer(String name) {
		int i = 0;
		for (i = 0; i < ServerPool.size(); i++) {
			if (ServerPool.get(i).getName().equalsIgnoreCase(name)) {
				try {
					ServerPool.get(i).getScoketpool().destroy();
				} catch (Exception e) {
					logger.error("destory " + name + " server error.", e);
				} finally {
					ServerPool.remove(i);
				}

			}
		}
	}

	public Server GetServer(String name) {
		for (Server s : ServerPool) {
			if (s.getName().equalsIgnoreCase(name)) {
				return s;
			}
		}
		return null;
	}

	public List<Server> GetAllServer() {
		return ServerPool;
	}

	/**
	 * 获取指定服务器外的服务器集合
	 * 
	 * */
	private String[] getNoName(String[] serverName) {
		String[] str = new String[ServerPool.size() - serverName.length];
		int count = 0;
		for (Server s : ServerPool) {
			for (String strName : serverName) {
				if (!s.getName().equals(strName)) {
					str[count++] = s.getName();
				}
			}
		}
		return str;
	}

}
