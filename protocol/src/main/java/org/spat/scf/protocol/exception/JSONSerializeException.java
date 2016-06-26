package org.spat.scf.protocol.exception;

public class JSONSerializeException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public JSONSerializeException() {
		super("服务器端数据JSON序列化错误!");
	}

	public JSONSerializeException(String message) {
		super(message);
		this.setErrCode(ReturnType.JSON_SERIALIZE_EXCEPTION);
	}
}
