package org.spat.scf.server.contract.context;

import org.spat.scf.protocol.sfp.Protocol;

/**
 * SCF request entity
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class SCFRequest {
	
	private Protocol protocol;

	private byte[] requestBuffer;
	
	public SCFRequest() {
		
	}
	
	public SCFRequest(Protocol protocol, byte[] buf) {
		super();
		this.protocol = protocol;
		this.requestBuffer = buf;
	}
	
	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public void setRequestBuffer(byte[] requestBuffer) {
		this.requestBuffer = requestBuffer;
	}

	public byte[] getRequestBuffer() {
		return requestBuffer;
	}
}