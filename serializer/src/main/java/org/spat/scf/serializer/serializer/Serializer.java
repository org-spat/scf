/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.serializer;

import java.nio.charset.Charset;

import org.spat.scf.serializer.component.SCFInStream;
import org.spat.scf.serializer.component.SCFOutStream;
import org.spat.scf.serializer.utility.ClassHelper;
import org.spat.scf.serializer.utility.TypeHelper;
import org.spat.scf.serializer.utility.UseVersion;

/**
 *
 * @author Administrator
 */
public class Serializer {

	private Charset _Encoder = Charset.forName("UTF-8");
	private static boolean isInit = false;
	private static Object locker = new Object();

	public static void Init() {
		if (!isInit) {
			synchronized (locker) {
				if (!isInit) {
					TypeHelper.InitTypeMap();
					isInit = true;
				}
			}
		}
	}
	
	public static void ReInit() {
		isInit = false;
	}

	public Serializer() {
		Init();
	}

	public Serializer(Charset encoder) {
		_Encoder = encoder;
		Init();
	}

	/*
	 * @return 对象序列化后的字节数组
	 * 
	 * @param obj 要序列化的对象
	 */
	public byte[] Serialize(Object obj) throws Exception {
		SCFOutStream stream = null;
		try {
			stream = new SCFOutStream();
			stream.Encoder = _Encoder;
			stream.WriteInt16(UseVersion.version);
			stream.WriteInt32(0);
			if (obj == null) {
				SerializerFactory.GetSerializer(null).WriteObject(null, stream); // 把null写入strean
			} else {
				Class<?> type = obj.getClass();
				if (obj instanceof ISCFSerializer) {// obj是否继承ISCFSerializer接口
					((ISCFSerializer) obj).Serialize(stream);
				} else {
					SerializerFactory.GetSerializer(type).WriteObject(obj, stream);
				}
			}
			byte[] result = stream.toByteArray();
			return result;
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}

	/*
	 * @return 对象序列化后的字节数组
	 * 
	 * @param obj 要序列化的对象
	 */
	public Object Derialize(byte[] buffer, Class<?> type) throws Exception {
		SCFInStream stream = null;
		try {
			stream = new SCFInStream(buffer);
			stream.Encoder = _Encoder;
			short version = stream.ReadInt16();
			int exVersion = stream.ReadInt32();
			if (exVersion == 0 && version == UseVersion.version) {
				if (ClassHelper.InterfaceOf(type, ISCFSerializer.class)) {
					ISCFSerializer obj = (ISCFSerializer) type.newInstance();
					obj.Derialize(stream);
					return obj;
				}
				return SerializerFactory.GetSerializer(type).ReadObject(stream, type);
			} else {
				throw new Exception("Please use serializer SCFV" + UseVersion.version);
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
}
