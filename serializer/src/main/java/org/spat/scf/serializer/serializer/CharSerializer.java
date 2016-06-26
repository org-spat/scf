/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.serializer;

import org.spat.scf.serializer.component.SCFInStream;
import org.spat.scf.serializer.component.SCFOutStream;
import org.spat.scf.serializer.utility.ByteHelper;

/**
 *
 * @author Administrator
 */
class CharSerializer extends SerializerBase {

    @Override
    public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
        byte[] bs = ByteHelper.GetBytesFromChar((Character) obj);
        for (byte b : bs) {
            outStream.WriteByte(b);
        }
    }

    @Override
    public Object ReadObject(SCFInStream inStream, Class defType) throws Exception {
        short data = inStream.ReadInt16();
        byte[] buffer = ByteHelper.GetBytesFromInt16(data);
        return ByteHelper.getCharFromBytes(buffer);
    }
}
