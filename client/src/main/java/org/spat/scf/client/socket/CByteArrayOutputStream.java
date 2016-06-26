package org.spat.scf.client.socket;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * CByteArrayOutputStream
 * 
 * @author Service Platform Architecture Team 
 */
public class CByteArrayOutputStream extends ByteArrayOutputStream {

	public byte[] toByteArray(int index, int len) {
        return Arrays.copyOfRange(buf, index, Math.min(index + len, size()));
    }
}
