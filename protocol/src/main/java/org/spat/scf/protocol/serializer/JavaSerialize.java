package org.spat.scf.protocol.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JavaSerialize extends SerializeBase {

	@Override
	public byte[] serialize(Object obj) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(stream);
		out.writeObject(obj);
		out.flush();
		byte[] buffer = stream.toByteArray();
		stream.close();
		out.close();
		return buffer;
	}

	@Override
	public Object deserialize(byte[] data, Class<?> cls) throws Exception {
		ByteArrayInputStream stream = new ByteArrayInputStream(data);
		ObjectInputStream in = new ObjectInputStream(stream);
		Object result = in.readObject();
		stream.close();
		in.close();
		return result;
	}

}
