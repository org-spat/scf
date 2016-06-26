/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.exception;

/**
 *
 * @author Administrator
 */
public class OutOfRangeException extends Exception {

    private String msg;

    public OutOfRangeException(String message) {
        msg = message;
    }

    public OutOfRangeException() {
        msg = "Out range exeception.";
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
