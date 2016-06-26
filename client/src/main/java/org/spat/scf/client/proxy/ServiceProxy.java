package org.spat.scf.client.proxy;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.spat.scf.client.SCFConst;
import org.spat.scf.client.configurator.ServiceConfig;
import org.spat.scf.client.loadbalance.Dispatcher;
import org.spat.scf.client.loadbalance.Server;
import org.spat.scf.client.loadbalance.ServerChoose;
import org.spat.scf.client.loadbalance.ServerState;
import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;
import org.spat.scf.client.manager.GetConfig;
import org.spat.scf.client.proxy.SSMRespData.SSMRespDataType;
import org.spat.scf.client.proxy.component.InvokeResult;
import org.spat.scf.client.proxy.component.Parameter;
import org.spat.scf.client.proxy.component.ReceiveHandler;
import org.spat.scf.client.utility.AsyncDetectHelper;
import org.spat.scf.client.utility.ScfKeyLoad;
import org.spat.scf.protocol.enumeration.CompressType;
import org.spat.scf.protocol.enumeration.PlatformType;
import org.spat.scf.protocol.enumeration.SDPType;
import org.spat.scf.protocol.enumeration.SerializeType;
import org.spat.scf.protocol.exception.RebootException;
import org.spat.scf.protocol.exception.ThrowErrorHelper;
import org.spat.scf.protocol.exception.TimeoutException;
import org.spat.scf.protocol.sdp.ExceptionProtocol;
import org.spat.scf.protocol.sdp.HandclaspProtocol;
import org.spat.scf.protocol.sdp.RequestProtocol;
import org.spat.scf.protocol.sdp.ResponseProtocol;
import org.spat.scf.protocol.sfp.Protocol;
import org.spat.scf.protocol.utility.KeyValuePair;

/**
 * ServiceProxy
 * 
 * @author Service Platform Architecture Team 
 */
public class ServiceProxy {

	private static final ILog logger = LogFactory.getLogger(ServiceProxy.class);
	private int count = 0;
	private int sessionId = 1;
    private int requestTime = 0;/**超时重连次数*/
    private int ioreconnect = 0;/**IO服务切换次数*/
    private ServiceConfig config;
	private Dispatcher dispatcher;
    private static final Object locker = new Object();
    private static final Object lockerSessionID = new Object();
    private static final HashMap<String, ServiceProxy> Proxys = new HashMap<String, ServiceProxy>();
    private static ConcurrentHashMap<String, ServerChoose> methodServer = new ConcurrentHashMap<String, ServerChoose>();
    private long configTime = 0L;
    private static List<String> remServices = new CopyOnWriteArrayList<String>();
    
    private ServiceProxy(String serviceName) throws Exception {
        config = ServiceConfig.GetConfig(serviceName);
        dispatcher = new Dispatcher(config);
        
        requestTime = config.getSocketPool().getReconnectTime();
    	int serverCount = 1;
    	if(dispatcher.GetAllServer() != null && dispatcher.GetAllServer().size() > 0){
    		serverCount = dispatcher.GetAllServer().size();
    	}
    	ioreconnect = serverCount - 1;
    	count = requestTime;
    	
    	if(ioreconnect > requestTime){
    		count = ioreconnect;
    	}
    }
    
    private ServiceProxy(String serviceName, String confXml) throws Exception {
        this.config = ServiceConfig.GetConfig(serviceName, confXml);
        dispatcher = new Dispatcher(config);
        
        requestTime = config.getSocketPool().getReconnectTime();
    	int serverCount = 1;
    	if(dispatcher.GetAllServer() != null && dispatcher.GetAllServer().size() > 0){
    		serverCount = dispatcher.GetAllServer().size();
    	}
    	ioreconnect = serverCount - 1;
    	count = requestTime;
    	
    	if(ioreconnect > requestTime){
    		count = ioreconnect;
    	}
    	if(!remServices.contains(serviceName)) {
    		remServices.add(serviceName);
    	}
    	ConfigUtil.getInstance();
    	
    	
    }
    
    private void destroy() {
    	List<Server> serverList = dispatcher.GetAllServer();
		if(serverList != null) {
			for(Server server : serverList) {
				try {
					server.getScoketpool().destroy();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
    }

    public static ServiceProxy getProxy(String serviceName) throws Exception {
        ServiceProxy p = Proxys.get(serviceName.toLowerCase());
       
        if (p == null) {
	        synchronized(locker) {
	        	p = Proxys.get(serviceName.toLowerCase());
	        	if (p == null) {
	        		String scfkey = ScfKeyLoad.getInstance().getContext();
	        		SSMRespData configData = null;
	        		
	        		if(null != scfkey) {
	        			configData = GetConfig.getInstance().getRespData(serviceName.toLowerCase(), 0, 2000, scfkey);
	        		}
	        		
	        		if(configData != null && configData.getFlag() == SSMRespDataType.NORMAL 
	        				&& configData.getIsConfigChanged() && configData.getConfig() != null) {
	        			logger.info("Server "+serviceName+"/"+configData.getServerName() + " scf config:" + configData.getConfig());
	        			p = new ServiceProxy(serviceName, configData.getConfig());
	        			p.setConfigTime(configData.getLastChangeTime());
	        		}else {
	        			logger.warn("Server "+serviceName+" hava not scfmanagement config,and will use local scf.config!");
	        			p = new ServiceProxy(serviceName);
	        		}
	        		Proxys.put(serviceName.toLowerCase(), p);
	        	}
	        }
        }
        return p;
    }
    
    public InvokeResult invoke(Parameter returnType, String typeName, String methodName, Parameter[] paras) throws Exception, Throwable {
    	long watcher = System.currentTimeMillis();
        List<KeyValuePair> listPara = new ArrayList<KeyValuePair>();
        for (Parameter p : paras) {
            listPara.add(new KeyValuePair(p.getSimpleName(), p.getValue()));
        }
        RequestProtocol requestProtocol = new RequestProtocol(typeName, methodName, listPara);
        Protocol sendP = new Protocol(createSessionId(), 
        		(byte) config.getServiceid(),
        		SDPType.Request,
        		CompressType.UnCompress,
        		config.getProtocol().getSerializerType(),
        		PlatformType.Java,
        		requestProtocol);
        
        Protocol receiveP = null;
        Server server = null;
        String methodPara[] = this.getMethodPara(typeName, methodName, paras);
        for(int i = 0; i <= count; i++){
        	server = this.getKeyServer(methodPara);
        	
        	if(server == null) {
        		logger.error("cannot get server");
                throw new Exception("cannot get server");
            }    
            try{
            	receiveP = server.request(sendP); 
            	SuccessHandle(server);
            	break;
            } catch(IOException io){
            	if(count == 0 || i == ioreconnect){
            		throw io;
            	}
            	if(i < count && i < ioreconnect) {
            		logger.error(server.getName()+" server has IOException,system will change normal server!");
            		continue;
            	}
            } catch(RebootException rb){
            	AsyncDetectHelper.detectRebootServer(server);
            	logger.debug("Time " + System.currentTimeMillis() + " receive Reset protocol, server " + server.getAddress() + "is marked as Reboot state!!");
            	if(count == 0 || i == ioreconnect){
            		throw new IOException("connect fail!");
            	}
            	if(i < count && i < ioreconnect) {
            		logger.error(server.getName()+" server has reboot,system will change normal server!");
            		continue;
            	}
            }catch(TimeoutException toex){
            	if(count == 0 || i == requestTime){
            		TimeOutHandle(server);
            		throw toex;
            	}
            	if(i < count && i < requestTime) {
            		TimeOutHandle(server);
            		logger.error(server.getName()+" server has TimeoutException,system will change normal server!");
            		continue;
            	}
            } catch(UnresolvedAddressException uaex){
            	/**无法完全解析给定的远程地址*/
            	AsyncDetectHelper.detectDeadServer(server);
            	logger.debug("Time " + System.currentTimeMillis() + " throw UnresolvedAddressException, server " + server.getAddress() + "is marked as Dead state!!");
            	throw uaex;
            } catch (Throwable ex){
            	logger.error("invoke other Exception", ex);
            	throw ex;
            }
    	}
       
        if(receiveP == null){
        	throw new Exception("userdatatype error!");
        }
        
        if (receiveP.getSDPType() == SDPType.Response) {
            ResponseProtocol rp = (ResponseProtocol)receiveP.getSdpEntity();
            logger.debug("invoke time:" + (System.currentTimeMillis() - watcher) + "ms");
            return new InvokeResult(rp.getResult(), rp.getOutpara());
        } else if(receiveP.getSDPType() == SDPType.Reset){ /**服务重启*/
        	logger.info(server.getName()+" server is reboot,system will change normal server!");
        	AsyncDetectHelper.detectRebootServer(server);
        	logger.debug("Time " + System.currentTimeMillis() + " receive Reset protocol, server " + server.getAddress() + "is marked as Reboot state!!");
        	return invoke(returnType, typeName, methodName, paras);
        }else if (receiveP.getSDPType() == SDPType.Exception) {
            ExceptionProtocol ep = (ExceptionProtocol)receiveP.getSdpEntity();
            throw ThrowErrorHelper.throwServiceError(ep.getErrorCode(), ep.getErrorMsg());
        } else {
            throw new Exception("userdatatype error!");
        }
    }
    
    /**
     * 指定序列化方式
     * */
    public InvokeResult invoke(Parameter returnType, String typeName, String methodName, Parameter[] paras, String serVersion) throws Exception, Throwable {
    	long watcher = System.currentTimeMillis();
        List<KeyValuePair> listPara = new ArrayList<KeyValuePair>();
        for (Parameter p : paras) {
            listPara.add(new KeyValuePair(p.getSimpleName(), p.getValue()));
        }
        RequestProtocol requestProtocol = new RequestProtocol(typeName, methodName, listPara);
        SerializeType serializerType = SerializeType.SCFBinary;
        if(serVersion.equalsIgnoreCase("SCF")){
        	serializerType = SerializeType.SCFBinary;
        }
        Protocol sendP = new Protocol(createSessionId(), 
        		(byte) config.getServiceid(),
        		SDPType.Request,
        		CompressType.UnCompress,
        		serializerType,
        		PlatformType.Java,
        		requestProtocol);
        
        Protocol receiveP = null;
        Server server = null;
        String methodPara[] = this.getMethodPara(typeName, methodName, paras);
        for(int i = 0; i <= count; i++){
        	server = this.getKeyServer(methodPara);
        	
        	if(server == null) {
        		logger.error("cannot get server");
                throw new Exception("cannot get server");
            }    
            try{
            	receiveP = server.request(sendP);
            	SuccessHandle(server);
            	break;
            } catch(IOException io){
            	if(count == 0 || i == ioreconnect){
            		throw io;
            	}
            	if(i < count && i < ioreconnect) {
            		logger.error(server.getName()+" server has IOException,system will change normal server!");
            		continue;
            	}
            } catch(RebootException rb){
            	AsyncDetectHelper.detectRebootServer(server);
            	logger.debug("Time " + System.currentTimeMillis() + " throw RebootException, server " + server.getAddress() + "is marked as Reboot state!!");
            	if(count == 0 || i == ioreconnect){
            		throw new IOException("connect fail!");
            	}
            	if(i < count && i < ioreconnect) {
            		logger.error(server.getName()+" server has reboot,system will change normal server!");
            		continue;
            	}
            }catch(TimeoutException toex){
            	if(count == 0 || i == requestTime){
            		TimeOutHandle(server);
            		throw toex;
            	}
            	if(i < count && i < requestTime) {
//            		添加超时处理操作
            		TimeOutHandle(server);
            		logger.error(server.getName()+" server has TimeoutException,system will change normal server!");
            		continue;
            	}
            } catch(UnresolvedAddressException uaex){
            	AsyncDetectHelper.detectDeadServer(server);
            	logger.debug("Time " + System.currentTimeMillis() + " throw UnresolvedAddressException, server " + server.getAddress() + "is marked as Dead state!!");
            	throw uaex;
            }catch (Throwable ex){
            	logger.error("invoke other Exception", ex);
            	throw ex;
            }
            
    	}
        
       
        
        if(receiveP == null){
        	throw new Exception("userdatatype error!");
        }
        
        if (receiveP.getSDPType() == SDPType.Response) {
            ResponseProtocol rp = (ResponseProtocol)receiveP.getSdpEntity();
            logger.debug("invoke time:" + (System.currentTimeMillis() - watcher) + "ms");
            return new InvokeResult(rp.getResult(), rp.getOutpara());
        } else if(receiveP.getSDPType() == SDPType.Reset){ /**服务重启*/
        	logger.info(server.getName()+" server is reboot,system will change normal server!");
        	AsyncDetectHelper.detectRebootServer(server);
        	logger.debug("Time " + System.currentTimeMillis() + " recive Reset protocol, server " + server.getAddress() + "is marked as Reboot state!!");
        	return invoke(returnType, typeName, methodName, paras);
        }else if (receiveP.getSDPType() == SDPType.Exception) {
            ExceptionProtocol ep = (ExceptionProtocol)receiveP.getSdpEntity();
            throw ThrowErrorHelper.throwServiceError(ep.getErrorCode(), ep.getErrorMsg());
        } else {
            throw new Exception("userdatatype error!");
        }
    }
    
    public void invoke(Parameter returnType, String typeName, String methodName, Parameter[] paras, ReceiveHandler rh) throws Exception, Throwable {
        List<KeyValuePair> listPara = new ArrayList<KeyValuePair>();
        for (Parameter p : paras) {
            listPara.add(new KeyValuePair(p.getSimpleName(), p.getValue()));
        }
        RequestProtocol requestProtocol = new RequestProtocol(typeName, methodName, listPara);
        Protocol sendP = new Protocol(createSessionId(), 
        		(byte) config.getServiceid(),
        		SDPType.Request,
        		CompressType.UnCompress,
        		config.getProtocol().getSerializerType(),
        		PlatformType.Java,
        		requestProtocol);

        Server server = null;
        String[] methodPara = this.getMethodPara(typeName, methodName, paras);
        for(int i = 0; i <= count; i++){
        	server = this.getKeyServer(methodPara);
        	if(server == null) {
        		logger.error("cannot get server");
                throw new Exception("cannot get server");
            }
            try{
            	rh.setServer(server);
            	server.requestAsync(sendP, rh);    
//            	添加 获得server请求成功时的操作
            	SuccessHandle(server);
            	break;
            } catch(IOException io){
            	if(count == 0 || i == ioreconnect){
            		throw io;
            	}
            	if(i < count && i < ioreconnect) {
            		logger.error(server.getName()+" server has IOException,system will change normal server!");
            		continue;
            	}
            } catch(RebootException rb){
            	AsyncDetectHelper.detectRebootServer(server);
            	logger.debug("Time " + System.currentTimeMillis() + " throw RebootException, server " + server.getAddress() + "is marked as Reboot state!!");
            	if(count == 0 || i == ioreconnect){
            		throw new IOException("connect fail!");
            	}
            	if(i < count && i < ioreconnect) {
            		logger.error(server.getName()+" server has reboot,system will change normal server!");
            		continue;
            	}
            }catch(TimeoutException toex){
            	if(count == 0 || i == requestTime){
//            		添加
            		TimeOutHandle(server);
            		throw toex;
            	}
            	if(i < count && i < requestTime) {
//            		添加
            		logger.error(server.getName()+" server has TimeoutException,system will change normal server!");
            		TimeOutHandle(server);
            		continue;
            	}
            } catch(UnresolvedAddressException uaex){
            	AsyncDetectHelper.detectDeadServer(server);
            	logger.debug("Time " + System.currentTimeMillis() + " throw UnresolvedAddressException, server " + server.getAddress() + "is marked as Dead state!!");
            	throw uaex;
            }catch (Throwable ex){
            	logger.error("invoke other Exception", ex);
            	throw ex;
            }
    	}
    }
    
    public void SuccessHandle(Server server) {
    	if (server.getWeight() < 10) {
    		server.setContinueSuccessTimes(server.getContinueSuccessTimes() + 1);
    	}
		server.setContinueTimeOutTimes(0);
    }
    
    /**
     * 当socket超时时对server的一些变量的设置
     * @param server 超時的server
     */
    public void TimeOutHandle(Server server) {
    	server.setContinueTimeOutTimes(server.getContinueTimeOutTimes() + 1);
    	server.setContinueSuccessTimes(0);
    	server.setTotalTimeOutTimes(server.getTotalTimeOutTimes() + 1);
    }
    
    /**
     * 权限协议
     * @param data 
     * @return Protocol
     * @throws Exception 
     */
    public Protocol createProtocol(HandclaspProtocol hp) throws Exception{
    	Protocol sendRightsProtocol = new Protocol(createSessionId(), 
        		(byte) config.getServiceid(),
        		SDPType.Request,
        		CompressType.UnCompress,
        		config.getProtocol().getSerializerType(),
        		PlatformType.Java,
        		hp);
    	return sendRightsProtocol;
    }
    
    /**
     *  get Server info
     * @param name Server name
     * @return if Server exist return Server info else return empty
     */
    public String getServer(String name) {
        Server server = dispatcher.GetServer(name);
        if (server == null) {
            return "";
        }
        return server.toString();
    }
    
    public static void destroyAll() {
    	Collection<ServiceProxy> spList = Proxys.values();
		if(spList != null) {
			for(ServiceProxy sp : spList) {
				sp.destroy();
			}
		}
    }
    
    private int createSessionId() {
        synchronized (lockerSessionID) {
            if (sessionId > SCFConst.MAX_SESSIONID) {
                sessionId = 1;
            }
            return sessionId++;
        }
    }
    /**
     * 设置方法固定到具体服务器
     * @param lookup 类名
     * @param serverName 服务器名
     * @throws Exception 
     * */
    public static void setServer(String lookup, String methodName, List<String> para, String[] serverName) throws Exception {

    	if(serverName != null) {
    		ServerChoose sc = new ServerChoose(serverName.length, serverName);
    		
    		StringBuffer sb = new StringBuffer();
        	sb.append(lookup);
        	sb.append(methodName);
        	if(para != null) {
		    	for(String str : para) {
		    		sb.append(str);
		    	}	
        	}
//        	if(!methodServer.containsKey(sb.toString())) {
    			methodServer.put(sb.toString(), sc);
//    		}

    	}else {
    		logger.error("serverName is null");
    		throw new Exception("para or serverName is null");
    	}
    }
    /**
     * 根据类名、方法名确定这一类方法在某些服务器上执行
     * 
     * */
    public static void setServer(String lookup, String methodName, String[] serverName) throws Exception {
    	if(serverName != null) {
    		ServerChoose sc = new ServerChoose(serverName.length, serverName);
    		
    		StringBuffer sb = new StringBuffer();
        	sb.append(lookup);
        	sb.append(methodName);
        	
//        	if(!methodServer.containsKey(sb.toString())) {
    			methodServer.put(sb.toString(), sc);
//    		}

    	}else {
    		logger.error("serverName is null");
    		throw new Exception("para or serverName is null");
    	}
    }
    
    /**
     * 根据类名确定这个类的所有方法，固定发送到具体服务器
     * 
     * */
    public static void setServer(String lookup, String[] serverName) throws Exception {
    	if(serverName != null) {
    		ServerChoose sc = new ServerChoose(serverName.length, serverName);
    		
    		StringBuffer sb = new StringBuffer();
        	sb.append(lookup);
        	
//        	if(!methodServer.containsKey(sb.toString())) {
    			methodServer.put(sb.toString(), sc);
//    		}

    	}else {
    		logger.error("serverName is null");
    		throw new Exception("para or serverName is null");
    	}
    }
    
    public String[] getMethodPara(String lookup, String methodName, Parameter[] paras) {
    	String[] str = new String[3];
    	StringBuffer sb = new StringBuffer();
    	sb.append(lookup);
    	str[2] = sb.toString();
    	sb.append(methodName);
    	str[1] = sb.toString();
    	if(paras != null && paras.length == 0) {
    		sb.append("null");
    	}
    	for(Parameter p : paras) {
    		sb.append(p.getSimpleName());
    	}
    	str[0] = sb.toString();
    	return str;
    }
    
    private Server getKeyServer(String[] key) {
    	Server server = null;
    	for(int i = 0; i < key.length; i++) {
	    	if(methodServer.containsKey(key[i])) {
	    		server = dispatcher.GetServer(methodServer.get(key[i]));
	    		if(server != null) {
	    			break;
	    		}
	    	}
    	}
    	if(server == null) {
    		server = dispatcher.GetServer();
    	}
    	
//    	if(server.getState() == ServerState.Dead) {
//    		if(!server.isTesting()) {
//    			if(server.testing()) {
//    				server.setTesting(true);
//    				server.setState(ServerState.Testing);
//    			}else {
//    				server.setTesting(false);
//    			}
//    		}
//    	}
    	return server;
    }
    
    public ServiceConfig getConfig() {
		return config;
	}

	public void setConfig(ServiceConfig config) {
		this.config = config;
	}
	
	
	
	public long getConfigTime() {
		return configTime;
	}

	public void setConfigTime(long configTime) {
		this.configTime = configTime;
	}



	private static class ConfigUtil{
		private static ConfigUtil instance = null;
		public static ConfigUtil getInstance() {
			if(instance == null) {
				synchronized (locker) {
					if(instance == null) {
						instance = new ConfigUtil();
					}
				}
			}
			return instance;
		}
		private ConfigUtil() {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(true) {
						try{
							for(String serviceName : remServices) {
								ServiceProxy proxy = Proxys.get(serviceName);
								try {
									SSMRespData configData = GetConfig.getInstance().getRespData(serviceName.toLowerCase(), proxy.getConfigTime(), 2000,ScfKeyLoad.getInstance().getContext());
									if(configData != null && configData.getIsConfigChanged()) {
										if(configData.getFlag() == SSMRespDataType.NORMAL){
											logger.info("Server "+serviceName.toLowerCase() + " new scf config:"  + configData.getConfig());
											ServiceProxy p = new ServiceProxy(serviceName, configData.getConfig());
											List<Server> oldServers = new ArrayList<Server>();
											oldServers.addAll(proxy.dispatcher.GetAllServer());
											List<Server> newServers = p.dispatcher.GetAllServer();
											proxy.setConfigTime(configData.getLastChangeTime());
											for(Server newser : newServers) {
												boolean flag = false;
												for(Server oldser : oldServers) {
													if(newser.getName().equalsIgnoreCase(oldser.getName())) {
														flag = true;		
													}
												}
												if(!flag) {
													proxy.dispatcher.addServer(newser);
												}
											}
											
											Iterator<Server> oldItor = oldServers.iterator();
											while (oldItor.hasNext()) {
												Server oldser = oldItor.next();
												boolean bNeedRemove = true;
												for(Server newser : newServers) {
													if(oldser.getName().equalsIgnoreCase(newser.getName())) {
														bNeedRemove = false;
														break;
													}
												}
												if(bNeedRemove){
													proxy.dispatcher.removeServer(oldser.getName());
													oldser.setState(ServerState.Deleted);
												}
											}
										} else if(configData.getFlag() == SSMRespDataType.CANCEL){
//											if(configData.getServerName().equals(serviceName)) {
//												//该用户没有对当前服务访问权限|取消了访问权限
//												logger.info("no permisson for service:" + serviceName);
//												List<Server> oldServers = new ArrayList<Server>();
//												oldServers.addAll(proxy.dispatcher.GetAllServer());
//												Iterator<Server> oldItor = oldServers.iterator();
//												while (oldItor.hasNext()) {
//													Server oldser = oldItor.next();
//													proxy.dispatcher.removeServer(oldser.getName());
//												}
//											} else {
											
//											}
										}
									}
								}catch (Exception e) {
									e.printStackTrace();
									logger.debug("get config error from management", e);
								}
							}
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}catch(Exception excep){
							excep.printStackTrace();
						}
					}
				}
			});
			thread.setName("configUtil thread");
			thread.setDaemon(true);
			thread.start();
		}
	}
}

