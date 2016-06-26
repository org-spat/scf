package org.spat.scf.protocol.exception;

public class TimeoutException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public TimeoutException() {
		super("服务器端服务调用超时出错!");
	}

	public TimeoutException(String message) {
		super(message);
		this.setErrCode(ReturnType.TIME_OUT);
	}
}
