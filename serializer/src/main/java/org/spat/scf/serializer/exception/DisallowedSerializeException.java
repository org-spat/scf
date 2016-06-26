/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.exception;

/**
 *
 * @author Administrator
 */
public class DisallowedSerializeException extends Exception {

    private String msg;

    public DisallowedSerializeException(String message) {
        msg = message;
    }

    public DisallowedSerializeException() {
        msg = "This type disallowed serialize,please add SCFSerializable attribute to the type.";
    }

    public DisallowedSerializeException(Class type) {
        msg = "This type disallowed serialize,please add SCFSerializable attribute to the type.type:" + type.getName();
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
