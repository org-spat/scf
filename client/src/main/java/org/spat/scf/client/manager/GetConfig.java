package org.spat.scf.client.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.spat.scf.client.SCFConst;
import org.spat.scf.client.proxy.SSMReqProtocol;
import org.spat.scf.client.proxy.SSMRespData;
import org.spat.scf.client.socket.ManagerSocket;

public class GetConfig {
	
	private static GetConfig instance = null;
	private static Object lock = new Object();
	private int manageSessionId = 1;
	private static final Object lockerManageSessionID = new Object();
	
	public static GetConfig getInstance() {
		if(instance == null) {
			synchronized (lock) {
				if(instance == null) {
					instance = new GetConfig();
				}
			}
		}
		return instance;
	}
	private CheckServerUtil checkUtil = new CheckServerUtil();
	
	private static int port = ManagerProperties.instance.getManagerPort();
	public List<ConfigServer> serverPool = new ArrayList<ConfigServer>();
	private static String[] ips = ManagerProperties.instance.getManagerIps();
	private static int COUNT = 0;
	private static int selected;
	
	private GetConfig() {
		for(String ip : ips) {
			ConfigServer server = new ConfigServer(ip, port);
			if(server.isFlag()) {
				serverPool.add(server);
				COUNT++;
			}
		}
		selected = new Random().nextInt(COUNT==0 ? 1 : COUNT) ;
	}
	
	private int getSelected() {
		for(int i = 0; i < COUNT; i++) {
			if(serverPool.get(selected % COUNT).isFlag()) {
				return selected;
			}else {
				selected = (selected + 1) % COUNT;
			}
		}
		return -1;
	}
	
	/**
	 * @param servicename
	 * @param time
	 * @param timeout
	 * @param scfkey scfkey
	 * @return
	 * @throws IOException
	 * @author haoxb
	 */
	public synchronized byte[] getConfigServicename(String servicename, long time, long timeout, String scfkey) throws IOException {
		if(null == scfkey) {
			return null;
		}
		StringBuffer req = new StringBuffer();
		
		int sessionId = createSessionId();
		//{sessionid}:{servicename}:{currentconfigtime}:{scfkey}
		req.append(sessionId).append(":");
		req.append(servicename).append(":").append(time);
		req.append(":").append(scfkey);
		
		int n = getSelected();
		if(n == -1) {
			return null;
		}		
		ConfigServer server = null;
		try {
			server = serverPool.get(n % COUNT);
			SSMReqProtocol reqProtocol = new SSMReqProtocol();
			reqProtocol.setType(0);
			byte[] sendData = reqProtocol.dataCreate(req.toString().getBytes());
			
			ManagerSocket socket = server.getSocket();
			socket.send(sendData);
			socket.registerRec(sessionId);
			byte[] data = socket.receive(sessionId,timeout);
			
			return data;
			
		} catch (IOException e) {
			if(null != server) {
				server.setFlag(false);
				server.getSocket().close();
				checkUtil.offer(server);
			}
			throw e;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public SSMRespData getRespData(String servicename, long time, long timeout, String scfkey) throws Exception{
		byte[] data = null;
		int count = COUNT;
		while(data == null && 0 != count--) {
			try {
				data = GetConfig.getInstance().getConfigServicename(servicename, time, timeout, scfkey);
			} catch (IOException e) {
				continue;
			}
		}
		
		SSMRespData resp = null;
		if(data != null) {
			resp = new SSMRespData();
			resp = SSMRespData.fromBytes(data);
		}
		return resp;
	}
	
	private int createSessionId() {
        synchronized (lockerManageSessionID) {
            if (manageSessionId > SCFConst.MAX_SESSIONID) {
            	manageSessionId = 1;
            }
            return manageSessionId++;
        }
    }
}
