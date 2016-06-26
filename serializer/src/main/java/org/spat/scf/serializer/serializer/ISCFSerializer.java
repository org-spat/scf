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
public interface ISCFSerializer {

    void Serialize(SCFOutStream outStream);

    void Derialize(SCFInStream inStream);
}
