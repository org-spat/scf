package org.spat.scf.protocol.compress;

class UnCompress extends CompressBase {

	@Override
	public byte[] unzip(byte[] buffer) throws Exception {
		return buffer;
	}

	@Override
	public byte[] zip(byte[] buffer) throws Exception {
		return buffer;
	}
}