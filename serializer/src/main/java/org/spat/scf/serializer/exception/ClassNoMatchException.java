/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.exception;

/**
 *
 * @author Administrator
 */
@SuppressWarnings("serial")
public class ClassNoMatchException extends Exception {

    private String msg;

    public ClassNoMatchException(String message) {
        msg = message;
    }

    public ClassNoMatchException() {
        msg = "Class error.";
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
