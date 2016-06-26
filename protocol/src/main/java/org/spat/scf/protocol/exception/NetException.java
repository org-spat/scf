package org.spat.scf.protocol.exception;

public class NetException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NetException() {
		super("服务器端网络错误!");
	}

	public NetException(String message) {
		super(message);
		this.setErrCode(ReturnType.NET);
	}
}
