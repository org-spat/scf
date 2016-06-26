package org.spat.scf.protocol.sdp;

import java.io.Serializable;

import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFSerializable;

/**
 * ResponseProtocol
 *
 * @author Service Platform Architecture Team
 * 
 */
@SCFSerializable(name="ResponseProtocol")
public class ResponseProtocol extends SDP implements Serializable {
	private static final long serialVersionUID = -7722167131158784375L;
	@SCFMember(sortId=2)
    private Object result;
    @SCFMember(sortId=1)
    private Object[] outpara;
    
    public ResponseProtocol() {
    	
    }

    public ResponseProtocol(Object result, Object[] outpara) {
		super();
		this.result = result;
		this.outpara = outpara;
	}
    
	public Object[] getOutpara() {
        return outpara;
    }

    public void setOutpara(Object[] outpara) {
        this.outpara = outpara;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
