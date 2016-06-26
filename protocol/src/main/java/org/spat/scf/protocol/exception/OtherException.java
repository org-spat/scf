package org.spat.scf.protocol.exception;

public class OtherException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public OtherException() {
		super("服务器端其他错误!");
	}

	public OtherException(String message) {
		super(message);
		this.setErrCode(ReturnType.OTHER_EXCEPTION);
	}
}
