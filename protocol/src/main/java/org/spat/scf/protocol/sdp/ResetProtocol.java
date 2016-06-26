package org.spat.scf.protocol.sdp;

import java.io.Serializable;

import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFSerializable;

@SCFSerializable(name="ResetProtocol")
public class ResetProtocol extends SDP implements Serializable {
	private static final long serialVersionUID = -2112939861828712708L;
	@SCFMember(sortId=1)
    private String msg;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
