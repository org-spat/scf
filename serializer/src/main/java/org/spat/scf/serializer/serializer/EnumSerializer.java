/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.serializer;

import org.spat.scf.serializer.component.SCFInStream;
import org.spat.scf.serializer.component.SCFOutStream;
import org.spat.scf.serializer.utility.TypeHelper;

/**
 *
 * @author Administrator
 */
class EnumSerializer extends SerializerBase {

    @Override
    public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
        int typeId = TypeHelper.GetTypeId(obj.getClass());
        outStream.WriteInt32(typeId);
        String value = obj.toString();
        SerializerFactory.GetSerializer(String.class).WriteObject(value, outStream);
    }

    @Override
    public Object ReadObject(SCFInStream inStream, Class defType) throws Exception {
        int typeId = inStream.ReadInt32();
        Class type = TypeHelper.GetType(typeId);
        if (type == null) {
            throw new ClassNotFoundException("Cannot find class with typId,target class:" + defType.getName() + ",typeId:" + typeId);
        }
        String value = (String) SerializerFactory.GetSerializer(String.class).ReadObject(inStream, defType);
        return Enum.valueOf(type, value);
    }
}
