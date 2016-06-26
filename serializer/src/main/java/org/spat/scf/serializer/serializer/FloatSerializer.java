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
class FloatSerializer extends SerializerBase {

    @Override
    public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
        int value = Float.floatToIntBits((Float)obj);
        outStream.WriteInt32(value);
    }

    @Override
    public Object ReadObject(SCFInStream inStream, Class defType) throws Exception {
        int value = inStream.ReadInt32();
        return Float.intBitsToFloat(value);
    }
}
