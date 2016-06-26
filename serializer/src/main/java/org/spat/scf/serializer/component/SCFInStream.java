/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.spat.scf.serializer.exception.StreamException;
import org.spat.scf.serializer.utility.ByteHelper;

/**
 *
 * @author Administrator
 */
public class SCFInStream extends ByteArrayInputStream implements Cloneable {

    public final int MAX_DATA_LEN = 1024 * 1024 * 10;
    public Charset Encoder = Charset.forName("UTF-8");
    private Map<Integer, Object> _RefPool = new HashMap<Integer, Object>();

    public SCFInStream(byte[] buffer) {
        super(buffer);
    }

    public SCFInStream(byte[] buffer, int offset, int length) {
        super(buffer, offset, length);
    }

    public void SafeRead(byte[] buffer) throws StreamException, IOException {
        if (this.read(buffer) != buffer.length) {
            throw new StreamException();
        }
    }

    public Object GetRef(int hashcode) {
        if (hashcode == 0) {
            return null;
        }
        return _RefPool.get(hashcode);
    }

    public void SetRef(int hashcode, Object obj) {
        _RefPool.put(hashcode, obj);
    }

    public short ReadInt16() throws Exception {
        byte[] buffer = new byte[2];
        if (this.read(buffer) != 2) {
            throw new StreamException();
        }
        return ByteHelper.ToInt16(buffer);
    }

    public int ReadInt32() throws Exception {
        byte[] buffer = new byte[4];
        if (this.read(buffer) != 4) {
            throw new StreamException();
        }
        return ByteHelper.ToInt32(buffer);
    }

    public long ReadInt64() throws Exception {
        byte[] buffer = new byte[8];
        if (this.read(buffer) != 8) {
            throw new StreamException();
        }
        return ByteHelper.ToInt64(buffer);
    }
    /*
     * 为了兼容protocol-1.6.0和1.6.1包中ResponseProtocol类的 sortid不一致导致的bug
     * 从二进制流中需要读取ResponseProtocol类时强制尝试两种转换方式
    */
    //*/
    public SCFInStream Clone() {
    	SCFInStream instream = null;
    	try {
			instream = (SCFInStream)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return instream;
    }
}
