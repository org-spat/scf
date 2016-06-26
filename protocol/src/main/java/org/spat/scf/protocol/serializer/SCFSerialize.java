package org.spat.scf.protocol.serializer;

import org.spat.scf.serializer.serializer.Serializer;
import org.spat.scf.serializer.utility.TypeHelper;

/**
 * ByteCodeSerialize
 *
 * @author Service Platform Architecture Team
 * 
 */
class SCFSerialize extends SerializeBase {

    private static Object serializer = null;
    
//    @Override
//    public void Init() 
//    {
//    	TypeHelper.InitTypeMap();
//    }

    @Override
    public byte[] serialize(Object obj) throws Exception {
    	if(serializer==null){
    		serializer = new Serializer();
    	}
        return ((Serializer)serializer).Serialize(obj);
    }

    @Override
    public Object deserialize(byte[] data, Class<?> cls) throws Exception {
    	if(serializer==null){
    		serializer = new Serializer();
    	}
        return ((Serializer)serializer).Derialize(data,cls);
    }
}