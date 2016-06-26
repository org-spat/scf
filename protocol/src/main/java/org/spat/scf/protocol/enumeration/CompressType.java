package org.spat.scf.protocol.enumeration;

/**
 * CompressType
 *
 * @author Service Platform Architecture Team 
 * 
 */
public enum CompressType {

    /**
     *  不压缩(无意义编号为0)
     */
    UnCompress(0),
    
    /**
     * 7zip
     */
    SevenZip(1),
    
    /**
     * DES加密
     */
    DES(2);
    
    private final int num;

    public int getNum() {
        return num;
    }

    private CompressType(int num) {
        this.num = num;
    }

    public static CompressType getCompressType(int num) throws Exception {
        for (CompressType type : CompressType.values()) {
            if (type.getNum() == num) {
                return type;
            }
        }
    	throw new Exception("末知的压缩格式");
    }
}
