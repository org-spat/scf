package org.spat.scf.client.utility;

import org.spat.scf.client.loadbalance.Server;
import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;

public class TimeOutHelper {

	private static ILog logger = LogFactory.getLogger(TimeOutHelper.class);
	/**
	 * 通过server中总请求次数和总的超时次数计算当前server的超时比例，并根据超时比例计算server的权值等级
	 * @param totalRequestTimes
	 * @param totalTimeOutTimes
	 * @return
	 */
    public static int WeightGrade(Server server) {
//    	weight grade 有10个等级 从10~1 和0
    	int oldWeight = server.getWeight();
    	int newWeight = oldWeight;
    	try {
    		float timeOutPer = (float) ((server.getTotalTimeOutTimes()*1.0) / server.getRequestTimes());
    		logger.debug(server.getAddress() + " timeout percentage is :" + timeOutPer);
        	if (timeOutPer < 0.08) {
        		newWeight = 10;
        	} else if (timeOutPer >= 0.08 && timeOutPer < 0.16) {
        		newWeight = 9;
        	} else if (timeOutPer >= 0.16 && timeOutPer < 0.24) {
        		newWeight = 8;
        	} else if (timeOutPer >= 0.24 && timeOutPer < 0.32) {
        		newWeight = 7;
        	} else if (timeOutPer >= 0.32 && timeOutPer < 0.40) {
        		newWeight = 6;
        	} else if (timeOutPer >= 0.40 && timeOutPer < 0.48) {
        		newWeight = 5;
        	} else if (timeOutPer >= 0.48 && timeOutPer < 0.56) {
        		newWeight = 4;
        	} else if (timeOutPer >= 0.56 && timeOutPer < 0.64) {
        		newWeight = 3;
        	} else if (timeOutPer >= 0.64 && timeOutPer < 0.72) {
        		newWeight = 2;
        	} else if (timeOutPer >= 0.72 && timeOutPer < 0.80) {
        		newWeight = 1;
        	} else if (timeOutPer >= 0.80 && timeOutPer < 1.0) {
        		newWeight = 0;
        	} 
    	} catch(Exception e) {
    		logger.error(e);
    		return oldWeight;
    	}
    
////    	保证一级一级的升 一级一级的降
    	if(oldWeight > newWeight) {
    		newWeight = oldWeight - 1;
    		logger.debug(server.getAddress()+ " from " + oldWeight + " drop to " + newWeight);
    	} else if (oldWeight < newWeight) {
    		newWeight = oldWeight + 1;
    		logger.debug(server.getAddress()+ " from " + oldWeight + " raise to " + newWeight);
    	}
    	return newWeight;
    }
    
    /**
     * 通过连续超时的次数来判定server weight值 连续超时的次数多于4次后会降权值等级 先增加超时次数 在进行判定
     * @param server
     * @param continueTimeOutTimes
     * @return
     */
    public static boolean DownWeightGrade(Server server) {
//    	weight grade 有十种等级  0为休眠状态
    	
    	if (server.getWeight() > 0 && server.getContinueTimeOutTimes() >= 16) {
    		server.setWeight(server.getWeight() - 1);
    		logger.debug(server.getAddress() + " continue timeout times is " + server.getContinueTimeOutTimes());
    		logger.debug(server.getAddress()+ " drop to " + server.getWeight());
			return true;
    	}
    	return false;
    }
    
    /**
     * 通过连续成功的次数来判定server weight值 连续成功的次数多于4次后悔降权值等级
     * @param server
     * @param continueTimeOutTimes
     * @return
     */
    public static boolean UpWeightGrade(Server server) {
//    	grade 有十种等级 和一种休眠等级
    	if (server.getWeight() < 10 && server.getContinueSuccessTimes() >= 16) {
    		server.setWeight(server.getWeight() + 1);
    		logger.debug(server.getAddress() + " continue success times is " + server.getContinueTimeOutTimes());
    		logger.debug(server.getAddress()+ " raise to " + server.getWeight());
    		return true;
    	}
    	return false;
    }
    
    /**
     * 由于server权值过低，休眠server
     * @param server
     */
    public static void sleepServer(Server server) {
    	server.setWeight(0);
    	server.setSleepTime(System.currentTimeMillis());
//    	当server的sleepTimeOut时间升到了16分钟时候 不在增加 如果再次休眠则只是保持这个休眠时间不变
    	if (server.getContinueDownWeight() == 1) {
        	if (server.getSleepTimeOut() >= server.getMaxSleepTimeOut()) {
        		server.setSleepTimeOut(server.getMaxSleepTimeOut());
        	} else {
        		server.setSleepTimeOut(server.getSleepTimeOut() * 2);
        	}
    	} else {
    		server.setSleepTimeOut(server.getBaseSleepTimeOut());
    	}
    	server.initWeight();
    	logger.debug("Time :" + System.currentTimeMillis() + " server " + server.getAddress() + " enter a sleeping state. The sleeping time is " + server.getSleepTimeOut());
//    	System.out.println("server 的信息如下：" + server.toString());
    }
    
    /**
     * 将休眠的server唤醒
     * @param server
     */
    public static void wakeUpServer(Server server) {
    	server.setWeight(5);
    	server.setSleepTime(0L);
    	server.initWeight();
    	server.setContinueDownWeight(2);
    	logger.debug("Time :" + System.currentTimeMillis() + " server " + server.getAddress() + " is waked up!! ");
//    	System.out.println("server 的信息如下：" + server.toString());
    }
}
