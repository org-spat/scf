package org.spat.scf.protocol.compress;

class SevenZip extends CompressBase {

	@Override
	public byte[] unzip(byte[] buffer) throws Exception {
		throw new UnsupportedOperationException("not supported 7zip!");
	}

	@Override
	public byte[] zip(byte[] buffer) throws Exception {
		throw new UnsupportedOperationException("not supported 7zip!");
	}
}