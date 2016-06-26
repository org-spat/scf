package org.spat.scf.client.utility;

/**
 * TimeSpanHelper
 *
 * @author Service Platform Architecture Team 
 */
public class TimeSpanHelper {

    public static int getIntFromTimeSpan(String timeSpan) {
        int returnint = 0;
        String[] times = timeSpan.split(":");
        if(times.length == 3) {
            returnint += Integer.parseInt(times[0]) * 60 * 60 * 1000;
            returnint += Integer.parseInt(times[1]) * 60 * 1000;
            returnint += Integer.parseInt(times[2]) * 1000;
        }
        return returnint;
    }

    public static String getTimeSpanFromInt(int timeSpan) throws Exception {
        throw new Exception("NotImplementedException");
    }
    
    /**
     * 00(秒)
     * 00:00(分:秒)
     * 00:00:00(时:分:秒)
     * 00:00:00:00(时:分:秒:毫秒)
     * @param timeSpan
     * @return
     */
    public static int getIntFromTimeMsSpan(String timeSpan) {
        int returnint = 0;
        String[] times = timeSpan.split(":");
        
        switch (times.length) {
			case 1:
				returnint += Integer.parseInt(times[0]) * 1000;
				break;
			case 2:
				returnint += Integer.parseInt(times[0]) * 60 * 1000;
	            returnint += Integer.parseInt(times[1]) * 1000;
				break;
			case 3:
				returnint += Integer.parseInt(times[0]) * 60 * 60 * 1000;
	            returnint += Integer.parseInt(times[1]) * 60 * 1000;
	            returnint += Integer.parseInt(times[2]) * 1000;
				break;
			case 4:
				returnint += Integer.parseInt(times[0]) * 60 * 60 * 1000;
		        returnint += Integer.parseInt(times[1]) * 60 * 1000;
		        returnint += Integer.parseInt(times[2]) * 1000;
		        returnint += Integer.parseInt(times[3]);
				break;
		}
        
        return returnint;
    }
}
