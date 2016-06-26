package org.spat.scf.protocol.sdp;

import org.spat.scf.protocol.serializer.SerializeBase;

public abstract class SDP {

	public SDP fromBuffer(byte[] buf,SerializeBase serizlizer) throws Exception {
		return (SDP)serizlizer.deserialize(buf, this.getClass());
	}
	
	public byte[] toBuffer(SDP sdp,SerializeBase serizlizer) throws Exception {
		return serizlizer.serialize(sdp);
	}
}
