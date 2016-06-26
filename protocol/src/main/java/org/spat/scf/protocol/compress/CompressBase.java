package org.spat.scf.protocol.compress;

import org.spat.scf.protocol.enumeration.CompressType;

public abstract class CompressBase {

	private static CompressBase sevenZip = new SevenZip();
	private static CompressBase unCompress = new UnCompress();
	
    public static CompressBase getInstance(CompressType ct) throws Exception {
        if (ct == CompressType.UnCompress) {
            return unCompress;
        } else if(ct == CompressType.SevenZip){
            return sevenZip;
        }
        
        throw new Exception("末知的压缩格式");
    }
	
	public abstract byte[] unzip(byte[] buffer) throws Exception;

	public abstract byte[] zip(byte[] buffer) throws Exception;
}
