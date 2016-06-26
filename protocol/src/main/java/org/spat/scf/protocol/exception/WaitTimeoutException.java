package org.spat.scf.protocol.exception;

public class WaitTimeoutException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public WaitTimeoutException()
	{
		
	}
	public WaitTimeoutException(String message)
	{
		this.message=message;
	}
	
	private String message="客户端等待可用连接超时了";
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


}
