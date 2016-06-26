package org.spat.scf.protocol.enumeration;

/**
 * SerializeType
 *
 * @author Service Platform Architecture Team 
 * 
 */
public enum SerializeType {

    JSON(1),
    JAVABinary(2),
    XML(3),
    SCFBinary(4);
    
    private final int num;

    public int getNum() {
        return num;
    }

    private SerializeType(int num) {
        this.num = num;
    }

    public static SerializeType getSerializeType(int num) {
        for (SerializeType type : SerializeType.values()) {
            if (type.getNum() == num) {
                return type;
            }
        }
        return null;
    }
}
