package org.spat.scf.protocol.serializer;

import org.spat.scf.protocol.enumeration.SerializeType;

public class SerializeBaseFactory {

	private static SCFSerialize scfSerialize = new SCFSerialize();

	private static JSONSerialize jsonSerialize = new JSONSerialize();
	
	private static JavaSerialize javaSerialize = new JavaSerialize();


	private static class SerializeBaseFactoryHolder {
		public static SerializeBaseFactory serializeBaseFactory = new SerializeBaseFactory();
	}

	public static SerializeBaseFactory getInstance() {
		return SerializeBaseFactoryHolder.serializeBaseFactory;
	}

	public SerializeBase getSerializeType(SerializeType serializeType) throws Exception {
		if (serializeType == SerializeType.SCFBinary) {
			return scfSerialize;
		}else if(serializeType== SerializeType.JAVABinary) {
			return javaSerialize;
		} else if (serializeType == SerializeType.JSON) {
			return jsonSerialize;
		} 
		throw new Exception("末知的序列化算法");
	}
}
