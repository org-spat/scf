/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.serializer;

import java.sql.Timestamp;
import java.util.Date;

import org.spat.scf.serializer.component.SCFInStream;
import org.spat.scf.serializer.component.SCFOutStream;
import org.spat.scf.serializer.exception.OutOfRangeException;
import org.spat.scf.serializer.utility.ByteHelper;

/**
 *
 * @author Administrator
 */
class DateTimeSerializer extends SerializerBase {

    private long TimeZone = 8 * 60 * 60 * 1000;

    @Override
    public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
        byte[] buffer = ConvertToBinary((Date) obj);
        outStream.write(buffer);
    }

    @Override
    public Object ReadObject(SCFInStream inStream, Class defType) throws Exception {
        byte[] buffer = new byte[8];
        inStream.SafeRead(buffer);
        Date date = GetDateTime(buffer);
        if (defType == java.sql.Timestamp.class) {
            return new Timestamp(date.getTime());
        } else if (defType == java.sql.Date.class) {
            return new java.sql.Date(date.getTime());
        } else if (defType == java.sql.Time.class) {
            return new java.sql.Time(date.getTime());
        }
        return date;
    }

    private byte[] ConvertToBinary(Date dt) {
        Date dt2 = new Date();
        dt2.setTime(0);//1970-1-1 00:00:00
        long rel = dt.getTime() - dt2.getTime();
        return ByteHelper.GetBytesFromInt64(rel + TimeZone);
    }

    private Date GetDateTime(byte[] buffer) throws OutOfRangeException {
        long rel = ByteHelper.ToInt64(buffer);
        Date dt = new Date(rel - TimeZone);
        return dt;
    }
}
