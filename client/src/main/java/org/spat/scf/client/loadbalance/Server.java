
package org.spat.scf.client.loadbalance;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.spat.scf.client.configurator.ServerProfile;
import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;
import org.spat.scf.client.proxy.component.ReceiveHandler;
import org.spat.scf.client.socket.CSocket;
import org.spat.scf.client.socket.ScoketPool;
import org.spat.scf.client.socket.ThreadRenameFactory;
import org.spat.scf.client.socket.WindowData;
import org.spat.scf.client.utility.AsyncDetectHelper;
import org.spat.scf.client.utility.RandomHelper;
import org.spat.scf.protocol.exception.TimeoutException;
import org.spat.scf.protocol.sfp.Protocol;

/**
 * Server
 *
 * @author Service Platform Architecture Team 
 */
public class Server {

    private static ILog logger = LogFactory.getLogger(Server.class);
    private int port;
    private int weight;
    private String name;
    private long deadTime;
    private String address;
    private int deadTimeout;
    private float weightRage;
    private int currUserCount;
    private ServerState state;
    private ScoketPool scoketpool;
    private boolean testing = false;
    private final ScheduledExecutorService scheduler;
    
//    新添加
    private static long baseSleepTimeOut = 1000 * 30;		//当权重降为0时，最小休眠时间
    private static long maxSleepTimeOut = 1000 * 60 * 4;	//当权重降为0时，最大休眠时间
    
    private int continueSuccessTimes;//记录连续成功的次数
	private int continueTimeOutTimes;//记录连续超时的次数
    private byte[] abandonArray;//记录应该抛弃访问的次数
    private AtomicInteger timesCount = new AtomicInteger(0);//统计次数
    private long sleepTime;//开始休眠的时间 同时也记录server工作时连续超时的次数
    private long sleepTimeOut; //休眠超时的时间
    private int requestTimes;//记录server获得到的总次数
    private int totalTimeOutTimes;//记录总的超时次数
    private int continueDownWeight;//记录server是否一直超时 2为初始状态 0为非连续超时 1为连续超时

    protected Server(ServerProfile config) {
        this.name = config.getName();
        this.address = config.getHost();
        this.port = config.getPort();
        this.weightRage = config.getWeithtRate();
        this.deadTimeout = config.getDeadTimeout();
        this.abandonArray = new byte[]{0,0,0,0,0,0,0,0,0,0};//这个数组的长度同最大权值的长度相等
        this.setWeight(10);
        
        initServer();
        
        if (this.weightRage >= 0) {
            this.state = ServerState.Normal;
        } else {
            this.state = ServerState.Disable;
        }
        scheduler = Executors.newScheduledThreadPool(2,new ThreadRenameFactory("Async "+this.getName()+"-Server Thread"));
    }
	
	public void initServer() {
		this.continueSuccessTimes = 0;
		this.continueTimeOutTimes = 0;
		this.sleepTime = 0L;
		this.sleepTimeOut = baseSleepTimeOut;
		this.requestTimes = 0;
		this.totalTimeOutTimes = 0;
		this.continueDownWeight = 2;
	}
	
	public void initWeight() {
		this.continueSuccessTimes = 0;
		this.continueTimeOutTimes = 0;
		this.requestTimes = 0;
		this.totalTimeOutTimes = 0;
	}
	
	public long getBaseSleepTimeOut() {
		return baseSleepTimeOut;
	}
	
	public long getMaxSleepTimeOut() {
		return maxSleepTimeOut;
	}

    public long getDeadTime() {
        return deadTime;
    }

    public void setDeadTime(long deadTime) {
        this.deadTime = deadTime;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
    
    public int getCurrUserCount() {
        return currUserCount;
    }

    public int getPort() {
        return port;
    }

    public ScoketPool getScoketpool() {
        return scoketpool;
    }

    protected void setScoketpool(ScoketPool scoketpool) {
        this.scoketpool = scoketpool;
    }

    public ServerState getState() {
        return state;
    }

    public synchronized void setState(ServerState state) {
        this.state = state;
    }

    public int getWeight() {
        return weight;
    }
    
    public void setWeight(int weight) {
    	logger.debug("update server " + name +":" + address + " weight to "+weight); 
    	this.weight = weight;
    	int total = getAbandonArray().length;
    	setAbandonArray(RandomHelper.randomGenerator(total, total - weight));
    }

    public float getWeightRage() {
        return weightRage;
    }

    public int getDeadTimeout() {
        return deadTimeout;
    }

    protected void setDeadTimeout(int deadTimeout) {
        this.deadTimeout = deadTimeout;
    }

    public boolean isTesting() {
		return testing;
	}
    
	public void setTesting(boolean testing) {
		this.testing = testing;
	}
	
    public int getContinueSuccessTimes() {
		return continueSuccessTimes;
	}

	public void setContinueSuccessTimes(int continueSuccessTimes) {
		this.continueSuccessTimes = continueSuccessTimes;
	}
	
	public void setContinueTimeOutTimes(int continueTimeOutTimes) {
		this.continueTimeOutTimes = continueTimeOutTimes;
	}

	public int getContinueTimeOutTimes() {
		return continueTimeOutTimes;
	}
	
	private void setAbandonArray(byte[] abandonArray) {
		this.abandonArray = abandonArray;
	}
	
	public byte[] getAbandonArray() {
		return abandonArray;
	}
	
	public int getTimesCount() {
		return timesCount.get();
	}
	
	public int timesCountGetAndIncrement() {
		return timesCount.getAndIncrement();
	}
	
	public int timesCountGetAndSet(int num) {
		return timesCount.getAndSet(num);
	}
	
	public int timesCountIncrementAndGet() {
		return timesCount.incrementAndGet();
	}
	
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}
	
	public long getSleepTime() {
		return sleepTime;
	}
	
    public long getSleepTimeOut() {
		return sleepTimeOut;
	}

	public void setSleepTimeOut(long sleepTimeOut) {
		this.sleepTimeOut = sleepTimeOut;
	}
	
    public int getTotalTimeOutTimes() {
		return totalTimeOutTimes;
	}

	public void setTotalTimeOutTimes(int totalTimeOutTimes) {
		this.totalTimeOutTimes = totalTimeOutTimes;
	}

	public int getRequestTimes() {
		return requestTimes;
	}

	public void setRequestTimes(int requestTimes) {
		this.requestTimes = requestTimes;
	}
	
	public int getContinueDownWeight() {
		return continueDownWeight;
	}

	public void setContinueDownWeight(int continueDownWeight) {
		this.continueDownWeight = continueDownWeight;
	}
	
	public ScheduledExecutorService getScheduler() {
		return this.scheduler;
	}

	public Protocol request(Protocol p) throws Exception, Throwable {
        if (state == ServerState.Dead) {
        	logger.warn("This proxy server is unavailable.state:" + state + "+host:" + address);
        	throw new Exception("This proxy server is unavailable.state:" + state + "+host:" + address);
        }
        increaseCU();
        CSocket socket = null;
        try {
            try {
            	socket = this.scoketpool.getSocket();
                byte[] data = p.toBytes(socket.isRights(),socket.getDESKey());
                socket.registerRec(p.getSessionID());
                socket.send(data);
            } catch (Throwable ex) {
                logger.error("Server get socket Exception", ex);
                throw ex;
            }finally {
            	if(socket != null){
            		socket.dispose();
            	}
            }
            byte[] buffer = socket.receive(p.getSessionID(), currUserCount);
            Protocol result = Protocol.fromBytes(buffer,socket.isRights(),socket.getDESKey());
            
            try{
            	socket.errorCountestore();
            }catch(Exception exce){
            	exce.printStackTrace();
            }
            
            return result;
        } catch (IOException ex) {
            logger.error("io exception", ex);
            if (socket == null || !socket.connecting()) {
            	if (!AsyncDetectHelper.test(this)) {
                	AsyncDetectHelper.detectDeadServer(this);
                	logger.debug("Time " + System.currentTimeMillis() + " server " + this.address + "is marked as Dead!!");
            	}
            }
            throw ex;
        } catch (Throwable ex) {
            logger.error("request other Exception", ex);
            if(ex instanceof TimeoutException){
	            if (socket == null || socket.connecting()){
	            	if(socket.errorCountAdd() > 25 && !AsyncDetectHelper.test(this)){
	            		AsyncDetectHelper.detectDeadServer(this);
	            		logger.debug("Time " + System.currentTimeMillis() + " server " + this.address + " is marked as Dead");
	            	}
	            	
	            }
            }
            throw ex;
        } finally {
//            if (state == ServerState.Testing) {
//                markAsDead();
//            }
            if (socket != null) {
                socket.unregisterRec(p.getSessionID());
            }
            decreaseCU();
        }
    }
    
    /**
     * 异步
     * @param p
     * @return
     * @throws Exception
     * @throws Throwable
     */
    public void requestAsync(Protocol p, ReceiveHandler receiveHandler) throws Exception, Throwable {
    	 if (state == ServerState.Dead) {
         	logger.warn("This proxy server is unavailable.state:" + state + "+host:" + address);
         	throw new Exception("This proxy server is unavailable.state:" + state + "+host:" + address);
         }
         increaseCU();
         CSocket socket = null;
         try {
             try {
             	socket = this.scoketpool.getSocket();
                byte[] data = p.toBytes(socket.isRights(),socket.getDESKey());
                WindowData wd = new WindowData(receiveHandler, socket, data, p.getSessionID());
                socket.registerRec(p.getSessionID(), wd);
                socket.offerAsyncWrite(wd);
             } catch (Throwable ex) {
                 logger.error("Server get socket Exception", ex);  
                 throw ex;
             }finally {
             	if(socket != null){
             		socket.dispose();
             	}
             }
         } catch (IOException ex) {
             logger.error("io exception", ex);
             if (socket == null || !socket.connecting()) {
             	if (!AsyncDetectHelper.test(this)) {
                	AsyncDetectHelper.detectDeadServer(this);
                	logger.debug("Time " + System.currentTimeMillis() + " server " + this.address + "is marked as Dead!!");
            	}
             }
             throw ex;
         } catch (Throwable ex) {
             logger.error("request other Exception", ex);
             throw ex;
         } finally {
             decreaseCU();
         }
    }
    
    @Override
    public String toString() {
       	StringBuffer strb = new StringBuffer();
       	String string = "Name:" + name + ",Address:" + address + ",Port:" + port + ",Weight:" + weight + ",State:" + state.toString() + ",CurrUserCount:" + currUserCount;
       	if (scoketpool != null) {
       		strb.append(",ScoketPool:" + scoketpool.count());
       	}
       	//    	新添加
       	strb.append(string);
    	strb.append("\nweight : " + weight);
    	strb.append("\ncontinueSuccessTimes : " + continueSuccessTimes);
    	strb.append("\ncontinueTimeOutTimes : " + continueTimeOutTimes);
    	strb.append("\nabandonTimes : ");
    	for (int ii = 0; ii < abandonArray.length; ii ++) {
    		strb.append(abandonArray[ii] + " ");
    	}
    	strb.append("\ntimesCount : " + timesCount);
    	strb.append("\nsleepTime : " + sleepTime);
    	strb.append("\nsleepTimeOut : " + sleepTimeOut);
    	strb.append("\nrequestTimes : " + requestTimes);
    	strb.append("\ntotalTimeOutTimes : " + totalTimeOutTimes);
    	strb.append("\ncontinueDownWeight : " + continueDownWeight);
    	strb.append("\n");
    	
		return strb.toString();
    }

    /**
     * Increase current user
     */
    private synchronized void increaseCU() {
        currUserCount++;
    }

    /**
     * Decrease current user
     */
    private synchronized void decreaseCU() {
        currUserCount--;
        if (currUserCount <= 0) {
            currUserCount = 0;
        }
    }

}
