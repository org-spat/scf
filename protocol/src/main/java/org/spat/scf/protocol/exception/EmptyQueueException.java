package org.spat.scf.protocol.exception;

/**
 * EmptyQueueException
 *
 * @author Service Platform Architecture Team 
 * 
 */
public class EmptyQueueException extends RuntimeException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    public EmptyQueueException(String err) {
        super(err);
    }

}
