package org.spat.scf.server.contract.context;

import java.util.List;

import org.spat.scf.protocol.entity.Out;

/**
 * SCF response entity
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class SCFResponse {
	
	private Object returnValue;

	private List<Out<?>> outParaList;
	
	private byte[] responseBuffer;
	
	
	public SCFResponse(){
		
	}
	
	
	public SCFResponse(String rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	public SCFResponse(int rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(Integer rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(long rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(Long rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(short rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(Short rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(float rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(Float rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(boolean rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(Boolean rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(double rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(Double rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	public SCFResponse(char rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(Character rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(byte rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	public SCFResponse(Byte rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	public SCFResponse(Object rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	
	
	public void setValue(Object rv, List<Out<?>> op) {
		this.setOutParaList(op);
		this.setReturnValue(rv);
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}

	public List<Out<?>> getOutParaList() {
		return outParaList;
	}

	public void setOutParaList(List<Out<?>> outParaList) {
		this.outParaList = outParaList;
	}

	public void setResponseBuffer(byte[] responseBuffer) {
		this.responseBuffer = responseBuffer;
	}

	public byte[] getResponseBuffer() {
		return responseBuffer;
	}
}