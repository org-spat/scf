package org.spat.scf.server.performance.monitor;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.spat.scf.server.contract.context.Global;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.contract.server.IServer;
import org.spat.scf.server.performance.exception.SerializeException;

public class MonitorWebCenter implements IServer {
	static ILog logger = LogFactory.getLogger(MonitorWebCenter.class);

	static MonitorUDPClient udp;

	@Override
	public void start() throws  Exception {
		logger.info("----------------monitor server start------------------");
		logger.info("-- monitor server send ip: "
				+ Global.getSingleton().getServiceConfig().getString("scf.server.monitor.sendIP"));
		logger.info("-- monitor server port: "
				+ Global.getSingleton().getServiceConfig().getInt("scf.server.monitor.sendPort"));
		logger.info("------------------------------------------------------");
		udp = MonitorUDPClient.getInstrance(
				Global.getSingleton().getServiceConfig().getString("scf.server.monitor.sendIP"),
				Global.getSingleton().getServiceConfig().getInt("scf.server.monitor.sendPort"), "utf-8");
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					
					MonitorWebCenter.control(udp);
					
				} catch (Exception e) {
					logger.error("init monitor server error", e);
				}
			}
		});
		thread.setName("SCF Monitor UDP send Thread");
		thread.start();
	}

	@Override
	public void stop() throws Exception {
		udp.close();
	}

	public static void control(MonitorUDPClient udp)  throws Exception {
		
		MonitorCount mc = new MonitorCount();
		MonitorMethodExecTime mmet = new MonitorMethodExecTime();
		MonitorProtocol countP = new MonitorProtocol(MonitorType.count,(short) 0);
		MonitorProtocol abandonP = new MonitorProtocol(MonitorType.abandon,(short) 0);
		MonitorProtocol frameExP = new MonitorProtocol(MonitorType.frameEx,(short) 0);
		String serviceName = Global.getSingleton().getServiceConfig().getString("scf.service.name");
		MonitorJVM mjvm = new MonitorJVM(udp, serviceName);
		int sendtime = Global.getSingleton().getServiceConfig().getInt("scf.server.monitor.timeLag");
		String sendStr;
		while (true) {
			try{
				Thread.sleep(sendtime < 3000 ? 0 : (sendtime - 3000));								
				//并发
				mc.initMCount();				
				Thread.sleep(1000);				
				sendStr = getSendStr(serviceName, "count", mc.getCount());
				if(sendStr != null) {
					udp.send(countP.dataCreate(sendStr.getBytes()));
				}
				
				getMaxCount(MonitorCount.getFromIP(), serviceName);
				
				//框架异常
				FrameExCount.initCount(0);				
				Thread.sleep(1000);				
				sendStr = getSendStr(serviceName, "frameex", FrameExCount.getCount());
				if(sendStr != null) {
					udp.send(frameExP.dataCreate(sendStr.getBytes()));
				}
				
				//抛弃
				AbandonCount.initCount(0);				
				Thread.sleep(1000);				
				sendStr = getSendStr(serviceName, "abandon", AbandonCount.getCount());
				if(sendStr != null) {
					udp.send(abandonP.dataCreate(sendStr.getBytes()));
				}

				mmet.computeAveTime();
				Map<String, Integer> methodExeTime = mmet.getmethodAvgTime();
				getMethodTimeStr(methodExeTime, serviceName);
				//jvm
				mjvm.jvmGc();
				mjvm.jvmGCUtil();
				mjvm.jvmThreadCount();
				mjvm.jvmMemory();
				mjvm.jvmHeapMemory();
				mjvm.jvmNoHeapMemory();
				mjvm.jvmLoad();				
			}catch(Exception ex){
				logger.error("control method error"+ex);
			}
		}
	}
	/**
	 * 具体ip访问次数
	 * 
	 * */
	public static void getMaxCount(Map<String, Integer> map, String serviceName) {
		MonitorProtocol protocol = new MonitorProtocol(MonitorType.count,(short) 0);
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object ip = entry.getKey();
			Object count = entry.getValue();
			StringBuffer sb = new StringBuffer();
			sb.append("ip:\t");
			sb.append((String) ip);
			sb.append("\t");
			sb.append((Integer) count);
			sb.append("\t");
			sb.append(serviceName);
			try {
				udp.send(protocol.dataCreate(sb.toString().getBytes()));
			} catch (IOException e) {
				logger.error(e);
			} catch (SerializeException e) {
				logger.error(e);
			}
		}
	}
	/**
	 * 构造发送字符串
	 * 
	 * */
	public static String getSendStr(String serviceName, String sendType, int count) {
		StringBuffer sb = new StringBuffer();
		sb.append(sendType); 
		sb.append("\t");
		sb.append(count);
		sb.append("\t");
		sb.append(serviceName);
		return sb.toString();
	}
	
	public static void getMethodTimeStr(Map<String, Integer> map, String serviceName) {
		MonitorProtocol protocol = new MonitorProtocol(MonitorType.methodTime,(short) 0);
		for (Map.Entry<String, Integer> m : map.entrySet()) {
			String methodName = m.getKey();
			Integer avgExeTime = m.getValue();
			StringBuffer sb = new StringBuffer();
			sb.append("methodName:\t");
			sb.append(methodName);
			sb.append("\t");
			sb.append(avgExeTime);
			sb.append("\t");
			sb.append(serviceName);
			try {
				udp.send(protocol.dataCreate(sb.toString().getBytes()));
			} catch (IOException e) {
				logger.error(e);
			} catch (SerializeException e) {
				logger.error(e);
			}
			logger.debug(sb.toString());
		}
	}

}