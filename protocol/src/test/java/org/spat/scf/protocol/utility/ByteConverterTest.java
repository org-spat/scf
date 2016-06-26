package org.spat.scf.protocol.utility;

import static org.junit.Assert.fail;

import org.junit.Test;

public class ByteConverterTest {

	@Test
	public void testBytesToIntLittleEndianByteArray() {
		int len = ByteConverter.bytesToIntLittleEndian(new byte[]{126,42,0,0});
		System.out.println(len);
		
		byte[] buf = ByteConverter.intToBytesLittleEndian(10234);
		for(int i=0; i<buf.length; i++) {
			System.out.println(buf[i]);
		}
		//-6#9 39#9 0#9 0#9
	}

	@Test
	public void testIntToBytesLittleEndian() {
		fail("Not yet implemented");
	}

	@Test
	public void testBytesToIntBigEndianByteArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testIntToBytesBigEndian() {
		fail("Not yet implemented");
	}

	@Test
	public void testBytesToIntLittleEndianByteArrayInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testBytesToIntBigEndianByteArrayInt() {
		fail("Not yet implemented");
	}

}
