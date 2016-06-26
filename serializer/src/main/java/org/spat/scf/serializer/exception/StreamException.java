/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.exception;

/**
 *
 * @author Administrator
 */
public class StreamException extends Exception {

    private String msg;

    public StreamException(String message) {
        msg = message;
    }

    public StreamException() {
        msg = "Stream error.";
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
