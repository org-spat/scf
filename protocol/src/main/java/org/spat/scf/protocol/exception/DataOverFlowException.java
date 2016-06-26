package org.spat.scf.protocol.exception;

public class DataOverFlowException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DataOverFlowException() {
		super("服务器端数据溢出错误!");
	}

	public DataOverFlowException(String message) {
		super(message);
		this.setErrCode(ReturnType.DATA_OVER_FLOW_EXCEPTION);
	}
}
