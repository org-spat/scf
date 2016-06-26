package org.spat.scf.server.register;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.spat.scf.protocol.utility.ByteConverter;

public class SSMReqProtocol {
	private static final int HEADLEN = 9;
	private byte version = 0x01;
	private int totalLen;
	private int type;
	private byte[] body;

	public SSMReqProtocol() {
	}

	public SSMReqProtocol(int type, byte[] body) {
		this.type = type;
		this.body = body;
	}

	public byte[] dataCreate() throws Exception {
		return dataCreate(body);
	}

	public byte[] dataCreate(byte[] recv) throws Exception {

		ByteArrayOutputStream stream = null;
		body = recv;
		try {
			stream = new ByteArrayOutputStream();
			if (body != null) {
				stream.write(ByteConverter.intToBytesBigEndian(HEADLEN
						+ body.length));
			} else {
				stream.write(ByteConverter.intToBytesBigEndian(HEADLEN));
			}
			stream.write(version);
			stream.write(ByteConverter.intToBytesBigEndian(type));

			if (body != null) {
				stream.write(body);
			}
			return stream.toByteArray();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					throw new Exception(e);
				}
			}
		}
	}

	public static SSMReqProtocol fromBytes(byte[] buf) throws Exception {
		int index = 0;

		int totalLen = ByteConverter.bytesToIntBigEndian(buf, index);
		index += 4;

		byte version = buf[index];
		index++;

		int type = ByteConverter.bytesToIntBigEndian(buf, index);
		index += 4;

		byte[] body = new byte[totalLen - HEADLEN];

		if (body.length > 0) {
			System.arraycopy(buf, index, body, 0, totalLen - HEADLEN);
		}
		SSMReqProtocol rp = new SSMReqProtocol();
		rp.setVersion(version);
		rp.setType(type);
		rp.setBody(body);

		return rp;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public int getTotalLen() {
		return totalLen;
	}

	public void setTotalLen(int totalLen) {
		this.totalLen = totalLen;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

}
