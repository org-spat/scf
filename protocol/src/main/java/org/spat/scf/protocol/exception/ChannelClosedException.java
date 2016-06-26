package org.spat.scf.protocol.exception;

public class ChannelClosedException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ChannelClosedException()
	{
		this("Channel连接已经断开了!");
	}
	public ChannelClosedException(String message)
	{
		super(message);
	}
}
