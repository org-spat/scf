/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.serializer;

import java.math.BigDecimal;

import org.spat.scf.serializer.component.SCFInStream;
import org.spat.scf.serializer.component.SCFOutStream;

/**
 *
 * @author Administrator
 */
class DecimalSerializer extends SerializerBase {

    @Override
    public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
        SerializerFactory.GetSerializer(String.class).WriteObject(obj.toString(), outStream);
    }

    @Override
    public Object ReadObject(SCFInStream inStream, Class defType) throws Exception {
        Object value = SerializerFactory.GetSerializer(String.class).ReadObject(inStream, String.class);
        if (value != null) {
            return new BigDecimal(value.toString());
        }
        return BigDecimal.ZERO;
    }
}
