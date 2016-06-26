package org.spat.scf.client.loadbalance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;
import org.spat.scf.client.utility.TimeOutHelper;

public class UpdateServer {
	
	private static UpdateServer _UpdateServer = null;
	private final static Object LockHelper = new Object();
	private TimeOutWorker timeOutWorker = null;
	private static List<List<Server>> AllServerPool= new ArrayList<List<Server>>();
	
	private UpdateServer() throws IOException {
		timeOutWorker = new TimeOutWorker();
		Thread thread = new Thread(timeOutWorker);
		thread.setName("UpdateServer-thread");
		thread.setDaemon(true);
		thread.start();
	}
	
	public static UpdateServer getInstance() throws IOException {
		if (null == _UpdateServer) {
			synchronized (LockHelper) {
				if (null == _UpdateServer) {
					_UpdateServer = new UpdateServer();
				}
			}
		}
		return _UpdateServer;
	}
	
	public static void closeUpdate() {
		TimeOutWorker.setControl(false);
	}

	public static void addAllServerPool(List<Server> serverPool) {
		AllServerPool.add(serverPool);
		
	}

	public static List<List<Server>> getAllServerPool() {
		return AllServerPool;
	}
	
}

class TimeOutWorker implements Runnable {
	
	private static ILog logger = LogFactory.getLogger(TimeOutWorker.class);
	private static boolean control = true;
	
	@Override
	public void run() {
		while(isControl()) {
			try {
				logger.debug("update daemon thread start to work...");
				for(List<Server> serverPool : UpdateServer.getAllServerPool()) {
					int beforeWeight = 0;
					boolean allEqual = true;
					boolean hasNoZero = false;
					boolean allLowWeight = true;
					Server server = serverPool.get(0);
					
					if (server.getWeight() != -1) {
						beforeWeight = checkAndComputeWeight(server);//存放的是前一个server的权值
						if (beforeWeight > 5) {
							allLowWeight = false;
						}
						CheckServerState(server);
					}
//					开始对server进行update 主要有两个任务
//					1 判断server是否应该休眠 或是休眠的server是否应该被唤醒
//					2判断serverPool中的所有的server是否具有相同的权值
					for(int ii = 1; ii < serverPool.size(); ii ++) {
						server = serverPool.get(ii);
						if (server.getWeight() != -1) {
							int tempWeight = checkAndComputeWeight(server);
//							如果server的weight不为0
							if (tempWeight == 0) {
								CheckServerState(server);
								continue;
							}
							if (tempWeight > 5) {
								allLowWeight = false;
							}
							if (allEqual) {
								if (tempWeight == 10) {
									hasNoZero = true;
									allEqual = false;
									continue;
								} else {
									if (tempWeight != beforeWeight && beforeWeight > 0) {
										allEqual = false;
									}
									beforeWeight = tempWeight;
									hasNoZero = true;
								}
							}
						}
					}
					
//					当所有的server的权值低于6，将所有的权值都做+5操作
					if (allLowWeight) {
						for (int jj = 0; jj < serverPool.size(); jj ++) {
							Server temp = serverPool.get(jj);
							if (temp.getWeight() > 0 && temp.getWeight() < 6) {
								temp.initServer();
								temp.setWeight(temp.getWeight() + 5);
							}
						}
						logger.debug("All the low weight do + 5 operation!");
					} else {
//						如果所有serve具有相同的权值 将权值都升到10
						if (allEqual && server.getWeight() != 10) {
							if (hasNoZero) {
								for (int jj = 0; jj < serverPool.size(); jj ++) {
									Server temp = serverPool.get(jj);
									if (temp.getWeight() > 0 && temp.getSleepTimeOut() <= (temp.getMaxSleepTimeOut()/2)) {
										temp.initServer();
										temp.setWeight(10);
									}
								}
								logger.debug("All the server which have the same weight were update to 10!!");
							} else {
								//所有的server的权值都为0或者是0和-1
								for (int jj = 0; jj < serverPool.size(); jj ++) {
									if (serverPool.get(jj).getWeight() == 0) {
										TimeOutHelper.wakeUpServer(serverPool.get(jj));
									}
								}
								logger.debug("All the server which is weight equal to 0 were waked up!!");
							}
						}
					}
					
				}
				Thread.sleep(1000 * 30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int checkAndComputeWeight(Server server) {
		if (server.getContinueTimeOutTimes() != 0 && server.getWeight() > 0) {
			TimeOutModify(server);
		} else {
			SuccessModify(server);
		}
		if (server.getWeight() == 0 && server.getSleepTime() == 0L) {
    		TimeOutHelper.sleepServer(server);
    	}
		server.initWeight();
		return server.getWeight();
	}

	public int TimeOutModify(Server server) {
		int oldWeight = server.getWeight();
		if (server.getWeight() > 0) {
	    	if(!TimeOutHelper.DownWeightGrade(server)) {
	    		if (server.getRequestTimes() > 100) {
	        		int newWeight = TimeOutHelper.WeightGrade(server);
	            	if (oldWeight != newWeight) {
	            		server.setWeight(newWeight);
	            	}
	    		}
	    	}
		}
		if (oldWeight > server.getWeight() && server.getContinueDownWeight() == 2) {
			server.setContinueDownWeight(1);
		}
    	return server.getWeight();
	}

	public int SuccessModify(Server server) {
		
		int oldWeight = server.getWeight();
		if (server.getWeight() > 0) {
	    	if(!TimeOutHelper.UpWeightGrade(server)) {
	    		if (server.getRequestTimes() > 100) {
	        		int newWeight = TimeOutHelper.WeightGrade(server);
	            	if (oldWeight != newWeight) {
	            		server.setWeight(newWeight);
	            	}
	    		}
	    	}
		}
		if (oldWeight < server.getWeight() && server.getContinueDownWeight() == 1) {
			server.setContinueDownWeight(0);
		}
    	return server.getWeight();
	}
	
	public boolean CheckServerState(Server server) {
		boolean change = false;//server保持原始的状态不变
		if (server.getWeight() == 0 && (System.currentTimeMillis() - server.getSleepTime()) >= server.getSleepTimeOut()) {
			TimeOutHelper.wakeUpServer(server);
			logger.info("server " + server.getAddress() + "is waked up !!");
			change = true;				
		}
		return change;
	}
	
	public static boolean isControl() {
		return control;
	}
	public static void setControl(boolean control) {
		TimeOutWorker.control = control;
	}
}
