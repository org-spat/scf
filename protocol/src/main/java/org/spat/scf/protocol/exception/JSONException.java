package org.spat.scf.protocol.exception;

public class JSONException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public JSONException() {
		super("服务器端JSON错误!!");
	}

	public JSONException(String message) {
		super(message);
		this.setErrCode(ReturnType.JSON_EXCEPTION);
	}
}
