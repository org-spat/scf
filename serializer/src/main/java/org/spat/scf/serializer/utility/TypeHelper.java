/*
 * and open the template in the editor.
 */
package org.spat.scf.serializer.utility;

import org.spat.scf.serializer.component.TypeMap;
import org.spat.scf.serializer.exception.DisallowedSerializeException;

/**
 *
 * @author Administrator
 */
public class TypeHelper {

    public static void InitTypeMap() {
        TypeMap.InitTypeMap();
    }

    public static int GetTypeId(Class type) throws DisallowedSerializeException {
        return TypeMap.getTypeId(type);
    }

    public static Class GetType(int typeId) {
        return TypeMap.getClass(typeId);
    }

    public static boolean IsPrimitive(Class type) {
        if (type.isPrimitive()) {
            return true;
        } else if (type == Long.class || type == long.class) {
            return true;
        } else if (type == Integer.class || type == int.class) {
            return true;
        } else if (type == Byte.class || type == byte.class) {
            return true;
        } else if (type == Short.class || type == short.class) {
            return true;
        } else if (type == Character.class || type == char.class) {
            return true;
        } else if (type == Double.class || type == double.class) {
            return true;
        } else if (type == Float.class || type == float.class) {
            return true;
        } else if (type == Boolean.class || type == boolean.class) {
            return true;
        }
        return false;
    }
}
