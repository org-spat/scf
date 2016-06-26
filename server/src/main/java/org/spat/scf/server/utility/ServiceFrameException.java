package org.spat.scf.server.utility;

import java.io.Serializable;

import org.spat.scf.protocol.sdp.RequestProtocol;
import org.spat.scf.protocol.utility.KeyValuePair;

/**
 * service frame exception use in proxy and proxyhandle
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
@SuppressWarnings("serial")
public class ServiceFrameException extends Exception implements Serializable {

	private ErrorState state;
	
	private String errorMsg;
	
	private String fromIP;
	
	private String toIP;
	
	private Object sdp;
	
	
	/**
	 * create exception
	 * @param errorMsg
	 * @param fromIP
	 * @param toIP
	 * @param mb
	 * @param state
	 * @param cause
	 */
	public ServiceFrameException(String errorMsg, 
									String fromIP, 
									String toIP, 
									Object sdp, 
									ErrorState state, 
									Throwable cause){
        super(errorMsg,cause);
        this.setState(state);
        this.setErrorMsg(errorMsg);
        this.setFromIP(fromIP);
        this.setToIP(toIP);
        this.setSdp(sdp);
    }

	public ServiceFrameException(String errorMsg, ErrorState state, Throwable cause){
        this(errorMsg, "", "", null, state, cause);
    }
	
	public ServiceFrameException(String errorMsg, ErrorState state){
		this(errorMsg, "", "", null, state, null);
    }
	
	public ServiceFrameException(ErrorState state, Throwable cause){
		this("", "", "", null, state, cause);
    }
	
	public ServiceFrameException(ErrorState state){
		this("", "", "", null, state, null);
    }
	
	@Override
	public void printStackTrace() {
		System.out.println("-------------------------begin-----------------------------");
		System.out.println("fromIP:" + this.getFromIP());
		System.out.println("toIP:" + this.getToIP());
		System.out.println("state:" + this.getState().toString());
		System.out.println("errorMsg:" + this.getErrorMsg());
		System.out.println("MessageBodyBase:");
		
		if(sdp.getClass() == RequestProtocol.class) {
			RequestProtocol request = (RequestProtocol)this.sdp;
			System.out.println("Server.Lookup:" + request.getLookup());
			System.out.println("Server.MethodName:" + request.getMethodName());
			System.out.println("Server.ParaKVList:");
			for(KeyValuePair kv : request.getParaKVList()){
				System.out.println("key:" + kv.getKey() + "---value:"+kv.getValue());
			}
		}
        super.printStackTrace();
        System.out.println("--------------------------end------------------------------");
    }
	

	public void setState(ErrorState state) {
		this.state = state;
	}

	public ErrorState getState() {
		return state;
	}

	public String getFromIP() {
		return fromIP;
	}

	public void setFromIP(String fromIP) {
		this.fromIP = fromIP;
	}

	public String getToIP() {
		return toIP;
	}

	public void setToIP(String toIP) {
		this.toIP = toIP;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public Object getSdp() {
		return sdp;
	}

	public void setSdp(Object sdp) {
		this.sdp = sdp;
	}
}