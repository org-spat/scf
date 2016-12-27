
package org.spat.scf.client.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.spat.scf.client.configurator.SocketPoolProfile;
import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;
import org.spat.scf.client.utility.AutoResetEvent;
import org.spat.scf.protocol.exception.DataOverFlowException;
import org.spat.scf.protocol.exception.ProtocolException;
import org.spat.scf.protocol.exception.TimeoutException;
import org.spat.scf.protocol.sfp.SFPStruct;
import org.spat.scf.protocol.utility.ByteConverter;
import org.spat.scf.protocol.utility.ProtocolConst;

/**
 * CSocket
 *
 * @author Service Platform Architecture Team 
 */
public class CSocket {

    private static final ILog logger = LogFactory.getLogger(CSocket.class);
    private Socket socket;
    private byte[] DESKey;/**DES密钥*/
    private boolean rights;/**是否启用认证*/
    private ScoketPool pool;
    private SocketChannel channel;
    private boolean _inPool = false;
    private boolean _connecting = false;
    private boolean waitDestroy = false;
    private SocketPoolProfile socketConfig;
    private DataReceiver dataReceiver = null;
    private ByteBuffer receiveBuffer, sendBuffer;
    private final Object sendLockHelper = new Object();
    private final Object receiveLockHelper = new Object();
    private CByteArrayOutputStream receiveData = new CByteArrayOutputStream();
    private ConcurrentHashMap<Integer, WindowData> WaitWindows = new ConcurrentHashMap<Integer, WindowData>();
    private static NIOHandler handler = null;
    private SocketWriteReadHandler writeReadHandler = null;
    private volatile AtomicInteger error_count = new AtomicInteger(0);
    private volatile AtomicBoolean isping = new AtomicBoolean(false);
    private String address;
    private int port;
    
    protected CSocket(String addr, int port, ScoketPool _pool, SocketPoolProfile config) throws Exception {
    	this.address = addr;
    	this.port = port;
        this.socketConfig = config;
        this.pool = _pool;
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().setSendBufferSize(config.getSendBufferSize());
        channel.socket().setReceiveBufferSize(config.getRecvBufferSize());
        receiveBuffer = ByteBuffer.allocate(config.getBufferSize());
        sendBuffer = ByteBuffer.allocate(config.getMaxPakageSize());
        channel.connect(new InetSocketAddress(addr, port));
        
        long begin = System.currentTimeMillis();
		while(true) {
			if((System.currentTimeMillis() - begin) > 2000) {
				channel.close();
				throw new Exception("connect to "+ addr +":"+port+" timeout：2000ms" );
			}
			channel.finishConnect();
			if(channel.isConnected()) {
				break;
			} else {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					logger.error(e);
				}
			}
		}
		
		socket = channel.socket();
		_connecting = true;
        dataReceiver = DataReceiver.instance();
        dataReceiver.RegSocketChannel(this);
        handler = NIOHandler.getInstance();
        writeReadHandler = new SocketWriteReadHandler(config.getReadWriteTimeout(),TimeUnit.SECONDS);
        writeReadHandler.channelAdded(this);
        
        logger.info("MaxPakageSize:" + config.getMaxPakageSize());
		logger.info("SendBufferSize:" + config.getSendBufferSize());
		logger.info("RecvBufferSize:" + config.getRecvBufferSize());
        logger.info("create a new connection :" + this.toString());
    }

    /**
     * send message
     * @param data
     * @return
     * @throws IOException
     * @throws Throwable
     */
    public int send(byte[] data) throws IOException, Throwable {
        try {
        	writeReadHandler.channelWrite(this);//update write time
            synchronized (sendLockHelper) {
                int pakageSize = data.length + ProtocolConst.P_START_TAG.length + ProtocolConst.P_END_TAG.length;
                if (sendBuffer.capacity() < pakageSize) {
                    throw new DataOverFlowException("数据包(size:" + pakageSize + ")超过最大限制,请修改或增加配置文件中的<SocketPool maxPakageSize=\"" + socketConfig.getMaxPakageSize() + "\"/>节点属性！");
                }
          
	            int count = 0;
	    		sendBuffer.clear();
	    		sendBuffer.put(ProtocolConst.P_START_TAG);
	    		sendBuffer.put(data);
	    		sendBuffer.put(ProtocolConst.P_END_TAG);
	    		sendBuffer.flip();
	    		
	    		int retryCount = 0;
	    		logger.debug("send buffer size:"+sendBuffer.remaining());
	    		while(sendBuffer.hasRemaining()) {
	    			count += channel.write(sendBuffer);
	    				
	    			if(retryCount++ > 30) {
	    				throw new Exception("retry write count(" + retryCount + ") above 30,count:"+count);
	    			}
	    		}
	    		return count;
            }
        } catch (IOException ex) {
            _connecting = false;
            throw ex;
        } catch (NotYetConnectedException ex) {
            _connecting = false;
            throw ex;
        }
    }

    /**
     * receive message
     * @param sessionId
     * @param queueLen
     * @return
     * @throws IOException
     * @throws TimeoutException
     * @throws Exception
     */
    public byte[] receive(int sessionId, int queueLen) throws IOException, TimeoutException, Exception {
        WindowData wd = WaitWindows.get(sessionId);
        if (wd == null) {
            throw new Exception("Need invoke 'registerRec' method before invoke 'receive' method!");
        }
        AutoResetEvent event = wd.getEvent();
        int timeout = getReadTimeout(socketConfig.getReceiveTimeout(), queueLen);
        if (!event.waitOne(timeout)) {
            throw new TimeoutException("ServiceName:["+this.getServiceName()+"],ServiceIP:["+this.getServiceIP()+"],Receive data timeout or error!timeout:" + timeout + "ms,queue length:" + queueLen);
        }
        byte[] data = wd.getData();
        int offset = SFPStruct.Version;
        int len = ByteConverter.bytesToIntLittleEndian(data, offset);
        if (len != data.length) {
            throw new ProtocolException("The data length inconsistent!datalen:" + data.length + ",check len:" + len);
        }
        return data;
    }
    
    private volatile int index = 0;
    private volatile boolean handling = false;
    
    /**
     * get receive message byte
     * @throws Exception 
     */
    protected void frameHandle() throws Exception {
        if (handling) {
            return;
        }
        synchronized (receiveLockHelper) {
            handling = true;
            try {
                if (waitDestroy && isIdle()) {
                    logger.info("Shrinking the connection:" + this.toString());
                    dispose(true);
                    return;
                }
                receiveBuffer.clear();
                try {
                	int re = channel.read(receiveBuffer);
                	if (re < 0) {
                		this.closeAndDisponse();
                		logger.error("server is close.this socket will close.");
                		return;
                	}
                } catch (IOException ex) {
                    _connecting = false;
                    throw ex;
                } catch (NotYetConnectedException e) {
                    _connecting = false;
                    throw e;
                }
                receiveBuffer.flip();
                if (receiveBuffer.remaining() == 0) {
                    return;
                }
                
                writeReadHandler.channelRead(this);//update write time
                
                while (receiveBuffer.remaining() > 0) {
                    byte b = receiveBuffer.get();
                    receiveData.write(b);
                    if (b == ProtocolConst.P_END_TAG[index]) {
                        index++;
                        if (index == ProtocolConst.P_END_TAG.length) {
                            byte[] pak = receiveData.toByteArray(ProtocolConst.P_START_TAG.length, receiveData.size() - ProtocolConst.P_END_TAG.length - ProtocolConst.P_START_TAG.length);
                            int pSessionId = ByteConverter.bytesToIntLittleEndian(pak, SFPStruct.Version + SFPStruct.TotalLen);
                            WindowData wd = WaitWindows.get(pSessionId);
                            if (wd != null) {
                            	if(wd.getFlag() == 0){
                            		wd.setData(pak);
                            		wd.getEvent().set();
                            	}else if(wd.getFlag() == 1){
                            		/**异步*/
                            		wd.getReceiveHandler().notify(pak);
                            		unregisterRec(pSessionId);
                            	}
                            }
                            index = 0;
                            receiveData.reset();
                            continue;
                        }
                    } else if (index != 0) {
                    	if(b == ProtocolConst.P_END_TAG[0]) {
                    		index = 1;
                    	} else {
                    		index = 0;
                    	}
                    }
                }
            } catch(Exception ex){
            	index = 0;
            	throw ex;
            }finally {
                handling = false;
            }
        }
    }
    
    public void registerRec(int sessionId) {
        AutoResetEvent event = new AutoResetEvent();
        WindowData wd = new WindowData(event);
        WaitWindows.put(sessionId, wd);
    }
    
    public void registerRec(int sessionId, WindowData wd) {
        WaitWindows.put(sessionId, wd);
    }

    public void unregisterRec(int sessionId) {
        WaitWindows.remove(sessionId);
    }
    
    public boolean hasSessionId(int sessionId) {
    	return WaitWindows.containsKey(sessionId);
    }
    
    public void closeAndDisponse(){
    	this.close();
		dispose(true);
    }
    
    public void close() {
        pool.release(this);
    }

    public void dispose() throws Exception {
        dispose(false);
    }

    public void dispose(boolean flag) {
        if (flag) {
            logger.warning("destory a connection");
            try {
                dataReceiver.UnRegSocketChannel(this);
                writeReadHandler.channelRemoved(this);//取消channel监听
            } catch (Exception e) {
				e.printStackTrace();
			} finally {
                pool.destroy(this);
            }
        } else {
            close();
        }
    }

    protected void disconnect() throws IOException {
        if (channel != null) {
            channel.close();
        }
        if(socket != null){
        	socket.close();
        }
        _connecting = false;
    }
    
    
    public void offerAsyncWrite(WindowData wd){
    	handler.offerWriteData(wd);
    }
    
    public int getTimeOut(int queueLen) {
    	return getReadTimeout(socketConfig.getReceiveTimeout(), queueLen);
    } 
    
    private int getReadTimeout(int baseReadTimeout, int queueLen) {
        if (!socketConfig.isProtected()) {
            return baseReadTimeout;
        }
        if (queueLen <= 0) {
            queueLen = 1;
        }
        int result = baseReadTimeout;
        int flag = (queueLen - 100) / 10;
        if (flag >= 0) {
            if (flag == 0) {
                flag = 1;
            }
            result = baseReadTimeout / (2 * flag);
        } else if (flag < -7) {
            result = baseReadTimeout - ((flag) * (baseReadTimeout / 10));
        }

        if (result > 2 * baseReadTimeout) {
            result = baseReadTimeout;
        } else if (result < 5) {
            result = 5;/**min timeout is 5ms*/
        }
        if (queueLen > 50) {
            logger.warn("--ServiceName:["+this.getServiceName()+"],ServiceIP:["+this.getServiceIP()+"],IsProtected:true,queueLen:" + queueLen + ",timeout:" + result + ",baseReadTimeout:" + baseReadTimeout);
        }
        return result;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (_connecting || (channel != null && channel.isOpen())) {
                dispose(true);
            }
        } catch (Throwable t) {
            logger.error("Pool Release Error!:", t);
        } finally {
            super.finalize();
        }
    }
    
    /**
     * Get Socket statu
     * @return Socket statu
     */
    public boolean connecting() {
        return _connecting;
    }

    protected boolean inPool() {
        return _inPool;
    }

    protected void setInPool(boolean inPool) {
        _inPool = inPool;
    }

    protected SocketChannel getChannle() {
        return channel;
    }

    /**
     * 该链接是否是空闲状态
     */
    protected boolean isIdle() {
        return !(WaitWindows.size() > 0);
    }

    protected void waitDestroy() {
        this.waitDestroy = true;
    }
    
    public boolean isRights() {
		return rights;
	}

	public void setRights(boolean rights) {
		this.rights = rights;
	}
	
	public byte[] getDESKey() {
		return DESKey;
	}

	public void setDESKey(byte[] dESKey) {
		DESKey = dESKey;
	}
    
	public String getServiceIP(){
		if(socket != null && !socket.isClosed()){
			try{
				return socket.getInetAddress().getHostAddress();
			}catch(Exception ex){
				return null;
			}
		}
		return null;
	}
	
	public String getServiceName(){
		if(pool != null){
			return pool.getServicename();
		}
		return null;
	}
	
	public int getConfigTime() {
		return getReadTimeout(socketConfig.getReceiveTimeout(), 1);
	}
	
    @Override
    public String toString() {
        try {
            return (socket == null) ? "" : socket.toString();
        } catch (Throwable ex) {
            return "Socket[error:" + ex.getMessage() + "]";
        }
    }
    /**
     *关闭接收数据线程 
     * */
    public static void closeRecv() {
    	try{
    		DataReceiver.closeRecv();
    	}catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public int errorCountAdd(){
    	return error_count.getAndIncrement();
    }
    
    public void errorCountestore(){
    	error_count.set(0);
    }
    
    protected boolean ping(){
    	boolean result = false;
    	if(isping.compareAndSet(false, true)){
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(this.address, this.port), 1000);
                socket.close();
                result = true;
            } catch (Exception e) {
            } finally {
                logger.debug("ping server :" + this.address + ":" + this.port + "--alive:" + result);
                isping.set(false);
            }
    	}
    	return result;
    }
}
