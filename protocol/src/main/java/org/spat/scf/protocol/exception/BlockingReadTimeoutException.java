package org.spat.scf.protocol.exception;


import java.io.InterruptedIOException;


public class BlockingReadTimeoutException extends InterruptedIOException {

    private static final long serialVersionUID = 356009226872649493L;

    /**
     * Creates a new instance.
     */
    public BlockingReadTimeoutException() {
    	
        super("客户端读取数据超时了！");
    }

    /**
     * Creates a new instance.
     */
    public BlockingReadTimeoutException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    /**
     * Creates a new instance.
     */
    public BlockingReadTimeoutException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public BlockingReadTimeoutException(Throwable cause) {
        initCause(cause);
    }
}

