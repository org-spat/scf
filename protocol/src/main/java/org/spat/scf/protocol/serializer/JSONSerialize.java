package org.spat.scf.protocol.serializer;


/**
 * JsonSerialize
 *
 * @author Service Platform Architecture Team 
 * 
 */
class JSONSerialize extends SerializeBase {
	
    @Override
    public byte[] serialize(Object obj) throws Exception {
            throw new UnsupportedOperationException("Not supported json serialize!");
    }

    @Override
    public Object deserialize(byte[] data, Class<?> cls) throws Exception {
        throw new UnsupportedOperationException("Not supported json serialize!");
    }
}