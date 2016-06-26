package org.spat.scf.protocol.sdp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.spat.scf.protocol.serializer.SerializeBase;
import org.spat.scf.protocol.utility.ByteConverter;
import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFSerializable;


/**
 * ExceptionProtocol
 *
 * @author Service Platform Architecture Team 
 * 
 * 4byte(ErrorCode) | 15byte(ToIP) | 15byte(FromIP) | nbyte(错误信息)
 *
 */
@SCFSerializable(name = "ExceptionProtocol")
public class ExceptionProtocol extends SDP implements Serializable {

	private static final long serialVersionUID = 797071859498836995L;
	
	@SCFMember(sortId=1)
    private int errorCode;
    @SCFMember(sortId=2)
    private String toIP;
    @SCFMember(sortId=3)
    private String fromIP;
    @SCFMember(sortId=4)
    private String errorMsg;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
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

	@Override
	public SDP fromBuffer(byte[] buf,SerializeBase serizlizer) throws IOException {
		this.errorCode = ByteConverter.bytesToIntLittleEndian(buf, 0);
		toIP = new String(buf, 4, 15, "UTF8");
		fromIP = new String(buf, 19, 15, "UTF8");
		errorMsg = new String(buf, 34, buf.length - 34, "UTF8"); 
		return this;
	}

	@Override
	public byte[] toBuffer(SDP sdp,SerializeBase serizlizer) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write(ByteConverter.intToBytesLittleEndian(errorCode));
		byte[] tem = new byte[15];
		byte[] buf = toIP.getBytes(Charset.forName("UTF8"));
		System.arraycopy(buf, 0, tem, 0, buf.length);
		stream.write(buf);
		tem = new byte[15];
		buf = fromIP.getBytes(Charset.forName("UTF8"));
		System.arraycopy(buf, 0, tem, 0, buf.length);
		stream.write(buf);
		buf = errorMsg.getBytes(Charset.forName("UTF8"));
		stream.write(buf);
		buf = stream.toByteArray();
		stream.close();
		return buf;
	}
}

