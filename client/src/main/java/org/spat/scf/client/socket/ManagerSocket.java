package org.spat.scf.client.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;
import org.spat.scf.client.manager.ConfigServer;
import org.spat.scf.client.utility.AutoResetEvent;
import org.spat.scf.protocol.exception.TimeoutException;
import org.spat.scf.protocol.utility.ByteConverter;
import org.spat.scf.protocol.utility.ProtocolConst;

public class ManagerSocket {
	private static final ILog logger = LogFactory.getLogger(ManagerSocket.class);
	
	private SocketChannel channel;
	private boolean _connecting = false;
	private Socket socket;
	private ByteBuffer receiveBuffer, sendBuffer;
	private ConfigServer server;
	
	//Objects for receive 
	private ConcurrentHashMap<Integer, WindowData> WaitWindows = new ConcurrentHashMap<Integer, WindowData>();
	private final Object receiveLockHelper = new Object();
	private CByteArrayOutputStream receiveData = new CByteArrayOutputStream();
	private ManagerDataReceiver managerDataReceiver = null;
	
	public ManagerSocket(ConfigServer server) throws Exception {
		this.server = server;
		channel = SocketChannel.open();
		channel.configureBlocking(false);
		receiveBuffer = ByteBuffer.allocate(2048);
		sendBuffer = ByteBuffer.allocate(1024);
		channel.connect(new InetSocketAddress(server.getIp(), server.getPort()));

		long begin = System.currentTimeMillis();
		while (true) {
			if ((System.currentTimeMillis() - begin) > 2000) {
				channel.close();
				throw new Exception("connect to " + server.getIp() + ":" + server.getPort() + " timeout：2000ms");
			}
			channel.finishConnect();
			if (channel.isConnected()) {
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
		managerDataReceiver = ManagerDataReceiver.instance();
		managerDataReceiver.RegSocketChannel(this);
	}

	public int send(byte[] data) throws IOException, Throwable {
		try {
			sendBuffer.clear();
			sendBuffer.put(ProtocolConst.P_START_TAG);
			sendBuffer.put(ByteConverter
					.intToBytesBigEndian(ProtocolConst.P_START_TAG.length + 4
							+ data.length
							+ProtocolConst.P_END_TAG.length
							));
			sendBuffer.put(data);
			sendBuffer.put(ProtocolConst.P_END_TAG);
			sendBuffer.flip();
			int count = 0;
			int retryCount = 0;

			while (sendBuffer.hasRemaining()) {
				count += channel.write(sendBuffer);

				if (retryCount++ > 30) {
					throw new Exception("retry write count(" + retryCount
							+ ") above 30");
				}
			}
			return count;
		} catch (IOException ex) {
			_connecting = false;
			throw ex;
		} catch (NotYetConnectedException ex) {
			_connecting = false;
			throw ex;
		}
	}
	
	public byte[] receive(int sessionId, long timeout) throws IOException, TimeoutException, Exception {
        WindowData wd = WaitWindows.get(sessionId);
        if (wd == null) {
            throw new Exception("Need invoke 'registerRec' method before invoke 'receive' method!");
        }
        AutoResetEvent event = wd.getEvent();
        if (!event.waitOne(timeout)) {
        	logger.warn("SCF Management server ip:["+this.getServiceIP()+"],Receive config timeout or error!timeout:" + timeout + "ms");
        	return null;
        }
        byte[] data = wd.getData();
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
                receiveBuffer.clear();
                try {
                	int re = channel.read(receiveBuffer);
                	if (re < 0) {
                		this.close();
                		logger.error("manager server is close.this socket will close.");
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
                
                while (receiveBuffer.remaining() > 0) {
                    byte b = receiveBuffer.get();
                    receiveData.write(b);
                    if (b == ProtocolConst.P_END_TAG[index]) {
                        index++;
                        if (index == ProtocolConst.P_END_TAG.length) {
                            byte[] pak = receiveData.toByteArray(ProtocolConst.P_START_TAG.length, receiveData.size() - ProtocolConst.P_END_TAG.length - ProtocolConst.P_START_TAG.length);
                            int pSessionId = ByteConverter.bytesToIntBigEndian(pak);
                            byte[] data = new byte[pak.length - 8];
                            System.arraycopy(pak, 8, data, 0, pak.length - 8);
                            WindowData wd = WaitWindows.get(pSessionId);
                            if (wd != null) {
                            	if(wd.getFlag() == 0){
                            		wd.setData(data);
                            		wd.getEvent().set();
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
    
//	public byte[] receive(String s, long timeout) {
//		CByteArrayOutputStream receiveData = new CByteArrayOutputStream();
//		;
//		byte[] bytes;
//		try {
//			int count = 0;
//			long start = System.currentTimeMillis();
//			while ((count = channel.read(receiveBuffer)) >= 0) {
//				receiveBuffer.flip();
//				if (receiveBuffer.remaining() == 0) {
//					receiveBuffer.clear();
//					if (System.currentTimeMillis() - start > timeout) {
//						System.out.println("############################s");
//						return null;
//					}
//					continue;
//				}
//				bytes = new byte[count];
//				receiveBuffer.get(bytes);
//				receiveData.write(bytes);
//				receiveBuffer.clear();
//				if (receiveData.toByteArray().length < ProtocolConst.P_START_TAG.length) {
//					return null;
//				}
//				byte[] headDelimiter = new byte[ProtocolConst.P_START_TAG.length];
//				System.arraycopy(receiveData.toByteArray(), 0, headDelimiter,
//						0, ProtocolConst.P_START_TAG.length);
//				if (ProtocolHelper.checkHeadDelimiter(headDelimiter)) {
//					int index = ProtocolConst.P_START_TAG.length;
//					int sessionId = ByteConverter.bytesToIntBigEndian(
//							receiveData.toByteArray(), index);
//					index += 4;
//					int totalLen = ByteConverter.bytesToIntBigEndian(
//							receiveData.toByteArray(), index);
//					index += 4;
//					byte[] requestBuffer = new byte[totalLen - index];
//					if (requestBuffer.length > 0) {
//						System.arraycopy(receiveData.toByteArray(), index,
//								requestBuffer, 0, totalLen - index);
//					}
//					return requestBuffer;
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			_connecting = false;
//		} finally {
//			try {
//				receiveData.close();
//			} catch (Exception ex) {
//			}
//		}
//		return null;
//	}

    public void registerRec(int sessionId) {
        AutoResetEvent event = new AutoResetEvent();
        WindowData wd = new WindowData(event);
        WaitWindows.put(sessionId, wd);
    }
    
	public void close() {
		try {
			if (channel != null) {
	            channel.close();
	        }
	        if(socket != null){
	        	socket.close();
	        }
	        _connecting = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SocketChannel getChannel() {
		return channel;
	}
	public boolean is_connecting() {
		return _connecting;
	}

	public void set_connecting(boolean _connecting) {
		this._connecting = _connecting;
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
}
