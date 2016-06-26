package org.spat.scf.server.performance.monitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.spat.scf.protocol.utility.ByteConverter;
import org.spat.scf.server.performance.exception.SerializeException;

public class MonitorProtocol {
	
	public final static int HEADER_LENGTH = 9;
	private byte version = 0x01;
	private int totalLen;
	private short type;
	private short exType;
	private byte body[];

	/**
	 * totalLen : 协议头的总长度 
	 * type： 发送消息的类别 0 count某段时间内的并发数, 1 jvm java虚拟机信息, 2
	 * abandon 被server抛弃数
	 * 
	 * exType： type下的小类别,目前只有jvm下有小的分类； 0 默认无分类, 1 jvmGc 获取内存使用情况 , 2 jvmGcutil
	 * 获取堆内存gc数据， 3 thread 线程数据， 4 class 加载类的数据， 5 memory获取JVM内存使用情况 6 heap
	 * 获取虚拟机的heap内存使用情况， 7 noheap 获取虚拟机的noheap内存使用情况，8 load负载情况。
	 */

	public MonitorProtocol() {
	}

	public MonitorProtocol(short type) {
		this.type = type;
	}

	public MonitorProtocol(short type, short exType) {
		this.type = type;
		this.exType = exType;
		this.totalLen = MPStruct.getHeadLength();
	}

	public byte[] dataCreate(byte[] recv) throws SerializeException {
		
		ByteArrayOutputStream stream = null;
		try {
			stream = new ByteArrayOutputStream();
			body = recv;
			if(body != null) {
				stream.write(ByteConverter.intToBytesBigEndian(HEADER_LENGTH + body.length));
			} else {
				stream.write(ByteConverter.intToBytesBigEndian(HEADER_LENGTH));
			}
			stream.write(version);
			stream.write(ByteConverter.shortToBytesBigEndian(this.type));
			stream.write(ByteConverter.shortToBytesBigEndian(this.exType));
			
			if(body != null) {
				stream.write(body);
			}
			return stream.toByteArray();
		} catch (Exception e) {
			throw new SerializeException(e);
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					throw new SerializeException(e);
				}
			}
		}
	}

	public static MonitorProtocol fromBytes(byte[] buf) throws Exception {
		int index = 0;
		
		int totalLen = ByteConverter.bytesToIntBigEndian(buf, index);
		index += 4;
		
		byte version = buf[index];
		index++;

		short type = ByteConverter.bytesToShortBigEndian(buf, index);
		index += 2;
		
		short exType = ByteConverter.bytesToShortBigEndian(buf, index);
		index += 2;
		
		byte[] body = new byte[totalLen - HEADER_LENGTH];
		
		if(body.length>0) {
			System.arraycopy(buf, index, body, 0, totalLen - HEADER_LENGTH);
		}
		
		MonitorProtocol mp = new MonitorProtocol();
		mp.setVersion(version);
		mp.setType(type);
		mp.setExType(exType);
		mp.setBody(body);
		return mp;
	}

	public byte getVersion() {
		return version;
	}
	
	public void setVersion(byte version) {
		this.version = version;
	}
	
	public int getTotalLen() {
		return totalLen;
	}

	public void setTotalLen(int totalLen) {
		this.totalLen = totalLen;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public short getExType() {
		return exType;
	}

	public void setExType(short exType) {
		this.exType = exType;
	}
	
	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}
}
