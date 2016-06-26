/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.serializer;

import org.spat.scf.serializer.exception.DisallowedSerializeException;
import org.spat.scf.serializer.utility.TypeHelper;

/**
 *
 * @author Administrator
 */
class SerializerFactory {

    private final static SerializerBase arraySerializer = new ArraySerializer();
    private final static SerializerBase boolSerializer = new BooleanSerializer();
    private final static SerializerBase byteSerializer = new ByteSerializer();
    private final static SerializerBase charSerializer = new CharSerializer();
    private final static SerializerBase dateTimeSerializer = new DateTimeSerializer();
    private final static SerializerBase decimalSerializer = new DecimalSerializer();
    private final static SerializerBase doubleSerializer = new DoubleSerializer();
    private final static SerializerBase enumSerializer = new EnumSerializer();
    private final static SerializerBase floatSerializer = new FloatSerializer();
    private final static SerializerBase int16Serializer = new Int16Serializer();
    private final static SerializerBase int32Serializer = new Int32Serializer();
    private final static SerializerBase int64Serializer = new Int64Serializer();
    private final static SerializerBase keyValueSerializer = new KeyValueSerializer();
    private final static SerializerBase listSerializer = new ListSerializer();
    private final static SerializerBase mapSerializer = new MapSerializer();
    private final static SerializerBase nullSerializer = new NullSerializer();
    private final static SerializerBase objectSerializer = new ObjectSerializer();
    private final static SerializerBase stringSerializer = new StringSerializer();
    private final static SerializerBase setSerializer = new SetSerializer();

    public static SerializerBase GetSerializer(Class<?> type) throws ClassNotFoundException, DisallowedSerializeException {
        if (type == null) {
            return nullSerializer;
        } else if (type.isEnum()) {
            return enumSerializer;
        }
        int typeId = TypeHelper.GetTypeId(type);
        SerializerBase serializer = null;
        switch (typeId) {
            case 0:
            case 1:
                serializer = nullSerializer;
                break;
            case 2:
                serializer = objectSerializer;
                break;
            case 3:
                serializer = boolSerializer;
                break;
            case 4:
                serializer = charSerializer;
                break;
            case 5:
            case 6:
                serializer = byteSerializer;
                break;
            case 7:
            case 8:
                serializer = int16Serializer;
                break;
            case 9:
            case 10:
                serializer = int32Serializer;
                break;
            case 11:
            case 12:
                serializer = int64Serializer;
                break;
            case 13:
                serializer = floatSerializer;
                break;
            case 14:
                serializer = doubleSerializer;
                break;
            case 15:
                serializer = decimalSerializer;
                break;
            case 16:
                serializer = dateTimeSerializer;
                break;
            case 18:
                serializer = stringSerializer;
                break;
            case 19:
            case 20:
            case 21:
                serializer = listSerializer;
                break;
            case 22:
                serializer = keyValueSerializer;
                break;
            case 23:
                serializer = arraySerializer;
                break;
            case 24:
            case 25:
                serializer = mapSerializer;
                break;
            case 26:
                serializer = setSerializer;
                break;
            default:
                serializer = objectSerializer;
        }
        return serializer;
    }
}
