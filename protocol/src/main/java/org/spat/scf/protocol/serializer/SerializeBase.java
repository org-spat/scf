package org.spat.scf.protocol.serializer;

import java.nio.charset.Charset;

import org.spat.scf.protocol.enumeration.SerializeType;

/**
 * SerializeBase
 *
 * @author Service Platform Architecture Team 
 * 
 */
public abstract class SerializeBase {
    
    private Charset encoder;

	public Charset getEncoder() {
        return encoder;
    }

    public void setEncoder(Charset encoder) {
        this.encoder = encoder;
    }

    public static SerializeBase getInstance(SerializeType serializeType) throws Exception {
       return SerializeBaseFactory.getInstance().getSerializeType(serializeType);
    }
    
    public void Init() {
		//Don't do anything
	}

    public abstract byte[] serialize(Object obj) throws Exception;

    public abstract Object deserialize(byte[] data, Class<?> cls) throws Exception;
}