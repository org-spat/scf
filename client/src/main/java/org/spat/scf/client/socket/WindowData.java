package org.spat.scf.client.socket;

import org.spat.scf.client.proxy.component.ReceiveHandler;
import org.spat.scf.client.utility.AutoResetEvent;


/**
 * WindowData
 * 
 * @author Service Platform Architecture Team 
 */
public class WindowData {

	AutoResetEvent _event;
	byte[] _data;
	private byte flag = 0;/**0:同步(默认值)1:异步*/
	private Exception exception;
	private long timestamp;
	private ReceiveHandler receiveHandler;
	private CSocket csocket;
	private byte[] sendData;
	private int sessionId;

	public WindowData(AutoResetEvent event) {
		_event = event;
	}
	
	public WindowData(ReceiveHandler receiveHandler,CSocket csocket) {
		this.flag = 1;
		this.receiveHandler = receiveHandler;
		this.csocket = csocket;
	}
	
	public WindowData(ReceiveHandler receiveHandler,CSocket csocket, byte[] sendData, int sessionId) {
		this.flag = 1;
		this.receiveHandler = receiveHandler;
		this.csocket = csocket;
		this.sendData = sendData;
		this.sessionId = sessionId;
		this.timestamp = System.currentTimeMillis();
	}

	public AutoResetEvent getEvent() {
		return _event;
	}

	public byte[] getData() {
		return _data;
	}

	public void setData(byte[] data) {
		_data = data;
	}

	/**
	 * @return the flag
	 */
	public byte getFlag() {
		return flag;
	}

	/**
	 * @param flag
	 *            the flag to set
	 */
	public void setFlag(byte flag) {
		this.flag = flag;
	}

	/**
	 * @return the exception
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * @param exception
	 *            the exception to set
	 */
	public void setException(Exception exception) {
		this.exception = exception;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public ReceiveHandler getReceiveHandler() {
		return receiveHandler;
	}

	public void setReceiveHandler(ReceiveHandler receiveHandler) {
		this.receiveHandler = receiveHandler;
	}

	public CSocket getCsocket() {
		return csocket;
	}

	public void setCsocket(CSocket csocket) {
		this.csocket = csocket;
	}
	

	public byte[] getSendData() {
		return sendData;
	}

	public void setSendData(byte[] sendData) {
		this.sendData = sendData;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}
	
	
}
