package org.spat.scf.server.performance.monitor;

public class MPStruct {
	 public static final int Version = 1;
	 public static final int TotalLen = 4;
	 public static final int Type = 2;
	 public static final int ExType = 2;
	 public static int HeadLength;
	 
	 public static int getHeadLength () {
		 HeadLength = Version + TotalLen + Type + ExType;
		 return HeadLength;
	 }
}
