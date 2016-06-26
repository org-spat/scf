/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.spat.scf.serializer.utility.ByteHelper;

/**
 * SCFOutStream
 */
public class SCFOutStream extends ByteArrayOutputStream {

    public Charset Encoder = Charset.forName("UTF-8");
    private Map<Integer, Object> _RefPool = new HashMap<Integer, Object>();

    public boolean WriteRef(Object obj) throws IOException {
        if (obj == null) {
            this.WriteByte((byte) 1);
            this.WriteInt32((int) 0);
            return true;
        }
        int objHashcode = getHashCode(obj);
        if (_RefPool.containsKey(objHashcode)) {
            WriteByte((byte) 1);
            WriteInt32(objHashcode);
            return true;
        } else {
            _RefPool.put(objHashcode, obj);
            WriteByte((byte) 0);
            WriteInt32(objHashcode);
            return false;
        }
        
//        _RefPool.put(objHashcode, obj);
//        WriteByte((byte) 0);
//        WriteInt32(objHashcode);
//        return false;
    }

    public void WriteByte(byte value) throws IOException {
        this.write(new byte[]{value});
    }

    public void WriteInt16(short value) throws IOException {
        byte[] buffer = ByteHelper.GetBytesFromInt16(value);
        this.write(buffer);
    }

    public void WriteInt32(int value) throws IOException {
        byte[] buffer = ByteHelper.GetBytesFromInt32(value);
        this.write(buffer);
    }

    public void WriteInt64(long value) throws IOException {
        byte[] buffer = ByteHelper.GetBytesFromInt64(value);
        this.write(buffer);
    }
    private int hashCode = 1000; //每次序列化都会产生一个SCFOutStream对象所以hashCode不会达到最大值。getHashCode也不用加锁
    private Map<Object, Integer> _objMap = new HashMap<Object, Integer>();
    
    private int getHashCode(Object obj) {
    	if (obj == null) {
            return 0;
        }
        if (_objMap.containsKey(obj) && obj == _objMap.get(obj)) {
        	return _objMap.get(obj);
        } else {
        	_objMap.put(obj, ++hashCode);
            return hashCode;
        }
    }

//    private int getHashCode(Object obj) {
//        if (obj == null) {
//            return 0;
//        } else {
//            if (_objMap.containsKey(obj) && checkKey(obj)) {
//                return _objMap.get(obj);
//            } else {
//                _objMap.put(obj, ++hashCode);
//                return hashCode;
//            }
//        }
//    }
//
//    private boolean checkKey(Object obj) {
//        Set<Object> keys = _objMap.keySet();
//        for (Object key : keys) {
//            if (key == obj) {
//                return true;
//            }
//        }
//        return false;
//    }
}
