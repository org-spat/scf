package org.spat.scf.client.configurator;

import org.spat.scf.client.SCFConst;
import org.spat.scf.client.utility.TimeSpanHelper;
import org.w3c.dom.Node;

/**
 * SocketPoolProfile
 *
 * @author Service Platform Architecture Team 
 */
public class SocketPoolProfile {

    private int minPoolSize;
    private int maxPoolSize;
    private int sendTimeout;
    private int receiveTimeout;
    private int waitTimeout;
    private boolean nagle;
    private int shrinkInterval;
    private int bufferSize;
    private int connectionTimeout = 3000;
    private int maxPakageSize;
    private boolean _protected;
	private int reconnectTime = 0;
	private int readWriteTimeout = 180;//默认3m 最小30s
	
	private int recvBufferSize;
	private int sendBufferSize;

   

	public SocketPoolProfile(Node node) {
        this.minPoolSize = Integer.parseInt(node.getAttributes().getNamedItem("minPoolSize").getNodeValue());
        this.maxPoolSize = Integer.parseInt(node.getAttributes().getNamedItem("maxPoolSize").getNodeValue());
        this.sendTimeout = TimeSpanHelper.getIntFromTimeSpan(node.getAttributes().getNamedItem("sendTimeout").getNodeValue().toString());
        //this.receiveTimeout = TimeSpanHelper.getIntFromTimeSpan(node.getAttributes().getNamedItem("receiveTimeout").getNodeValue().toString());
        this.receiveTimeout = TimeSpanHelper.getIntFromTimeMsSpan(node.getAttributes().getNamedItem("receiveTimeout").getNodeValue().toString());
        this.waitTimeout = TimeSpanHelper.getIntFromTimeSpan(node.getAttributes().getNamedItem("waitTimeout").getNodeValue().toString());
        this.nagle = Boolean.parseBoolean(node.getAttributes().getNamedItem("nagle").getNodeValue().toString());
        this.shrinkInterval = TimeSpanHelper.getIntFromTimeSpan(node.getAttributes().getNamedItem("autoShrink").getNodeValue().toString());
        this.bufferSize = Integer.parseInt(node.getAttributes().getNamedItem("bufferSize").getNodeValue());
        if (bufferSize < SCFConst.DEFAULT_BUFFER_SIZE) {
            bufferSize = SCFConst.DEFAULT_BUFFER_SIZE;
        }
        Node nProtected = node.getAttributes().getNamedItem("protected");
        if (nProtected == null) {
            this._protected = SCFConst.DEFAULT_PROTECTED;
        } else {
            this._protected = Boolean.parseBoolean(nProtected.getNodeValue());
        }
        Node nPackage = node.getAttributes().getNamedItem("maxPakageSize");
        if (nPackage == null) {
            this.maxPakageSize = SCFConst.DEFAULT_MAX_PAKAGE_SIZE;
        } else {
            this.maxPakageSize = Integer.parseInt(nPackage.getNodeValue());
        }
        
        //Node nReconnectTime = node.getAttributes().getNamedItem("reconnectTimeout");
        //由于reconnectTimeout有歧义，字段变为reconnectTime
        Node nReconnectTime = node.getAttributes().getNamedItem("reconnectTime");
        if (nReconnectTime != null) {
            this.reconnectTime = Integer.parseInt(nReconnectTime.getNodeValue());
            if(reconnectTime < 0){
            	reconnectTime = 0;
            }
        }
        
        Node nReadWriteTimeout = node.getAttributes().getNamedItem("readWriteTimeout");
        if (nReadWriteTimeout != null) {
            this.readWriteTimeout = Integer.parseInt(nReadWriteTimeout.getNodeValue());
            if(readWriteTimeout < 0){
            	readWriteTimeout = 180;
            }else if(readWriteTimeout < 30){
            	readWriteTimeout = 30;
            }
        }
        
        this.recvBufferSize = 1024 * 1024 * 8;
		this.sendBufferSize = 1024 * 1024 * 8;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public boolean isNagle() {
        return nagle;
    }

    public int getReceiveTimeout() {
        return receiveTimeout;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public int getShrinkInterval() {
        return shrinkInterval;
    }

    public int getWaitTimeout() {
        return waitTimeout;
    }

    public boolean AutoShrink() {
        return shrinkInterval > 0;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * @return the maxPakageSize
     */
    public int getMaxPakageSize() {
        return maxPakageSize;
    }

    /**
     * @return the _protected
     */
    public boolean isProtected() {
        return _protected;
    }
    public int getReconnectTime() {
		return reconnectTime;
	}

	public int getRecvBufferSize() {
		return recvBufferSize;
	}

	public void setRecvBufferSize(int recvBufferSize) {
		this.recvBufferSize = recvBufferSize;
	}

	public int getSendBufferSize() {
		return sendBufferSize;
	}

	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

	public int getReadWriteTimeout() {
		return readWriteTimeout;
	}

	public void setReadWriteTimeout(int readWriteTimeout) {
		this.readWriteTimeout = readWriteTimeout;
	}
	
}
