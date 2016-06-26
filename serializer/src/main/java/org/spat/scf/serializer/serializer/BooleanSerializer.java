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
class BooleanSerializer extends SerializerBase {

    @Override
    public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
        byte value = 0;
        if ((Boolean) obj) {
            value = 1;
        }
        outStream.WriteByte(value);
    }

    @Override
    public Object ReadObject(SCFInStream inStream, Class defType) throws Exception {
        return inStream.read() > 0;
    }
}
