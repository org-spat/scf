package org.spat.scf.serializer.serializer;

import org.spat.scf.serializer.component.SCFInStream;
import org.spat.scf.serializer.component.SCFOutStream;

/**
 *
 * @author Administrator
 */
abstract class SerializerBase {

    public abstract void WriteObject(Object obj, SCFOutStream outStream) throws Exception;

    public abstract Object ReadObject(SCFInStream inStream, Class defType) throws Exception;
}
