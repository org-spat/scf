package org.spat.scf.protocol.enumeration;

/**
 * PlatformType
 *
 * @author Service Platform Architecture Team 
 * 
 */
public enum PlatformType {

//	Java(1),
//  Dotnet(2),
//  C(3);
	/**
	 * 由于c#客户端对应编码与服务器端不同，故做修改，修改与c#、java客户端一一对应
	 */
    Dotnet(0),
    Java(1),
    C(2);

    private final int num;

    public int getNum() {
        return num;
    }

    private PlatformType(int num) {
        this.num = num;
    }

    public static PlatformType getPlatformType(int num) {
        for (PlatformType type : PlatformType.values()) {
            if (type.getNum() == num) {
                return type;
            }
        }
        return null;
    }
}
