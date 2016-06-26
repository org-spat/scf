package org.spat.scf.protocol.sdp;

import java.io.Serializable;
import java.util.List;

import org.spat.scf.protocol.utility.KeyValuePair;
import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFSerializable;

/**
 * RequestProtocol
 *
 * @author Service Platform Architecture Team 
 * 
 */
@SCFSerializable(name="RequestProtocol")
public class RequestProtocol extends SDP implements Serializable  {

	private static final long serialVersionUID = -2684616496718418808L;
	
	@SCFMember(sortId=1)
    private String lookup;
    @SCFMember(sortId=2)
    private String methodName;
    @SCFMember(sortId=3)
    private List<KeyValuePair> paraKVList;

    public RequestProtocol() {
    }

    public RequestProtocol(String lookup, String methodName, List<KeyValuePair> paraKVList) {
        this.lookup = lookup;
        this.methodName = methodName;
        this.paraKVList = paraKVList;
    }

    public String getLookup() {
        return lookup;
    }

    public void setLookup(String lookup) {
        this.lookup = lookup;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<KeyValuePair> getParaKVList() {
        return paraKVList;
    }

    public void setParaKVList(List<KeyValuePair> paraKVList) {
        this.paraKVList = paraKVList;
    }
}
