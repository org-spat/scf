package org.spat.scf.protocol.exception;

public class ParaException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ParaException() {
		super("服务器端方法调用参数错误!");
	}

	public ParaException(String message) {
		super(message);
		this.setErrCode(ReturnType.PARA_EXCEPTION);
	}
}
