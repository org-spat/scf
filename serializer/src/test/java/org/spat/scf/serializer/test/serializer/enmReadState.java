/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.test.serializer;

import org.spat.scf.serializer.annotation.SCFSerializable;

/**
 *
 * @author Administrator
 */
@SCFSerializable
public enum enmReadState {

    Read(0),
    UnRead(1),
    All(2);
    private final int eNum;

    public int getENum() {
        return this.eNum;
    }

    private enmReadState(int stateNum) {
        this.eNum = stateNum;
    }
}
