/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.serializer;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.spat.scf.serializer.component.SCFInStream;
import org.spat.scf.serializer.component.SCFOutStream;
import org.spat.scf.serializer.exception.ClassNoMatchException;
import org.spat.scf.serializer.exception.StreamException;
import org.spat.scf.serializer.utility.TypeHelper;

/**
 *
 * @author Administrator
 */
class ListSerializer extends SerializerBase {

    @Override
    public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
        if (obj == null) {
            SerializerFactory.GetSerializer(null).WriteObject(null, outStream);
        }
        List list = (List) obj;
        int typeId = TypeHelper.GetTypeId(List.class);
        outStream.WriteInt32(typeId);
        if (outStream.WriteRef(obj)) {
            return;
        }
        outStream.WriteInt32(list.size());
        for (Object item : list) {
            if (item == null) {
                SerializerFactory.GetSerializer(null).WriteObject(item, outStream);
            } else {
                Class itemType = item.getClass();
                outStream.WriteInt32(TypeHelper.GetTypeId(itemType));
                SerializerFactory.GetSerializer(itemType).WriteObject(item, outStream);
            }
        }
    }

    @Override
    public Object ReadObject(SCFInStream inStream, Class defType) throws Exception {
        int typeId = inStream.ReadInt32();
        if (typeId == 0) {
            return null;
        }
        byte isRef = (byte) inStream.read();
        int hashcode = inStream.ReadInt32();
        if (isRef > 0) {
            return inStream.GetRef(hashcode);
        }
        int len = inStream.ReadInt32();
        if (len > inStream.MAX_DATA_LEN) {
            throw new StreamException("Data length overflow.");
        }
        Class type = TypeHelper.GetType(typeId);
        if (type == null) {
            throw new ClassNotFoundException("Cannot find class with typId,target class:" + defType.getName() + ",typeId:" + typeId);
        }
        if (type != List.class) {
            throw new ClassNoMatchException("Class must be list!type:" + type.getName());
        }
        int modifier = defType.getModifiers();
        if (!Modifier.isAbstract(modifier) && !Modifier.isInterface(modifier) && List.class.isAssignableFrom(defType)) {
            type = defType;
        } else {
            type = ArrayList.class; //default list type
            if (!defType.isAssignableFrom(type)) {
                throw new ClassNoMatchException("Defind type and value type not match !defind type:" + defType.getName() + ",value type:" + type.getName());
            }
        }
        List list = (List) type.newInstance();
        for (int i = 0; i < len; i++) {
            int itemTypeId = inStream.ReadInt32();
            if (itemTypeId == 0) {
//                list.set(i, null);
                list.add(null);
            } else {
                Class itemType = TypeHelper.GetType(itemTypeId);
                if (itemType == null) {
                    throw new ClassNotFoundException("Cannot find class with typId,target class:(list[item])" + ",typeId:" + itemTypeId);
                }
                Object value = SerializerFactory.GetSerializer(itemType).ReadObject(inStream, itemType);
                list.add(value);
            }
        }
        inStream.SetRef(hashcode, list);
        return list;
    }
}
