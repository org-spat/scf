/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.serializer;

import org.spat.scf.serializer.component.SCFInStream;
import org.spat.scf.serializer.component.SCFOutStream;

/**
 *
 * @author Administrator
 */
class DoubleSerializer extends SerializerBase {

    @Override
    public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
        long value = Double.doubleToLongBits((Double) obj);
        outStream.WriteInt64(value);
    }

    @Override
    public Object ReadObject(SCFInStream inStream, Class defType) throws Exception {
        long value = inStream.ReadInt64();
        return Double.longBitsToDouble(value);
    }
}
